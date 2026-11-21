---
title: Tasks
nav_order: 5
---

# Tasks
{: .no_toc }

1. TOC
{:toc}

Everything the robot does is a **task**. Examples include moving a servo, driving to a target, or
running a whole autonomous routine. Tasks are built into a tree and the tree is run once per loop.
Nothing ever blocks, so many things can happen at once.

## The three methods

Every task extends `Task` and implements:

| Method | Contract |
| --- | --- |
| `boolean run()` | Called once per loop. Do a little work and return quickly. Return **`true` when the task is finished**, `false` to keep going. Tasks that never end always return `false`. |
| `void end()` | Called if the task is stopped before it finishes, for example when it loses a `ParallelRaceTask` or its `TeleopTask` button is released. Put the robot in a safe state, such as motors off. It isn't called on a task that has already finished. |
| `Task reset()` | Return a brand-new copy of the task, as if it had never run. |

A task also needs a constructor that stores the opMode in the inherited `opMode` field
(`this.opMode = opMode;`), so it can reach the hardware.

{: .warning }
Never `sleep()` or loop inside `run()`. The whole robot shares one loop, so a task that blocks
freezes odometry and every other task.

### The `@Ends` annotation

Put `@Ends` above a task class if its `run()` is **guaranteed to eventually return `true`**:

```java
@Ends
public class SetClaw extends Task { ... }
```

Peregrine reads it through `task.ends()`. `TeleopTask` uses this to make sure a task it runs to
completion can actually complete. Compound tasks (`SeriesTask`, `ParallelTask`, `ParallelRaceTask`)
work it out from their children instead, so you never annotate those.

## Built-in tasks

### Combining tasks

| Task | Runs its children… | Finishes when… |
| --- | --- | --- |
| `SeriesTask(a, b, c, …)` | one at a time, in order | the last one finishes |
| `ParallelTask(a, b, c, …)` | all at once | **all** of them have finished |
| `ParallelRaceTask(a, b, c, …)` | all at once | **any one** of them finishes; the others are then ended |

All of these accept any number of tasks, and they nest freely:

```java
return new SeriesTask(
        new Drive(this, "pickup"),
        new ParallelTask(
                new Drive(this, "score_left"),
                new RaiseLift(this)          // your own task
        ),
        new Drive(this, "park")
);
```

### Waiting and one-off actions

| Task | What it does |
| --- | --- |
| `WaitTask(opMode, ms)` | Waits the given number of milliseconds, counted from its first run, then finishes. |
| `WaitUntilTask(() -> condition)` | Finishes on the first loop the condition is `true`. |
| `InstantTask(() -> code)` | Runs the code once and finishes immediately. |
| `EmptyTask()` | Does nothing and finishes immediately. |
| `IdleTask()` | Does nothing and never finishes. Keeps a tree (and so the opMode) running until STOP. |

`WaitUntilTask` and `InstantTask` take a **lambda**, a small piece of code written inline:

```java
new SeriesTask(
        new InstantTask(() -> hardware.claw.setPosition(0)),     // close the claw
        new WaitTask(this, 300),                                  // give it 0.3 s
        new WaitUntilTask(() -> hardware.liftSwitch.isPressed()), // wait for a touch sensor
        new InstantTask(() -> hardware.claw.setPosition(1))       // open it again
);
```

A race against a `WaitTask` gives any task a time limit:

```java
new ParallelRaceTask(new Drive(this, "park"), new WaitTask(this, 3000))  // give up after 3 s
```

### Driving

| Task | What it does |
| --- | --- |
| `Drive(opMode, "target")` | Drives to a named target from the SD card tables, then finishes. See [Driving to targets](driving-to-targets.md). |
| `PIDHold(opMode, "target")` | Holds the robot on a target with the honing PID. Never finishes on its own. `Drive` uses it internally. |
| `TeleopMovement(opMode)` | Robot-centric mecanum driving from gamepad 1: left stick to drive and strafe, right stick to turn. Never finishes. |

### Teleop

`TeleopTask(opMode, task, () -> button, togglable)` runs a task when a button is pressed. It never
finishes on its own.

| `togglable` | Behaviour |
| --- | --- |
| `false` | The task runs **while the button is held**. Releasing the button ends it early. |
| `true` | One press starts the task and it **runs until it finishes**, even after the button is released. The task must be one that ends (`@Ends`); otherwise the constructor throws. |

Either way, the task is reset afterwards so the next press starts it fresh. Combine several with
`TeleopMovement` in a `ParallelTask`. Here `SetClaw` and `SpinIntake` are your own tasks (see
[Writing your own task](#writing-your-own-task)):

```java
@Override
public Task defineTasks() {
    return new ParallelTask(
            new TeleopMovement(this),
            new TeleopTask(this, new SetClaw(this, 0), () -> gamepad1.a, true),
            new TeleopTask(this, new SetClaw(this, 1), () -> gamepad1.b, true),
            new TeleopTask(this, new SpinIntake(this), () -> gamepad1.right_bumper, false)  // held
    );
}
```

### Logging

`Logger(opMode)` writes a CSV file to the SD card with one row per loop. Add columns before it runs,
then run it alongside your other tasks:

```java
Task myRoutine = new SeriesTask(/* ...your routine... */);
Logger logger = new Logger(this);
logger.addLogItem("lift", () -> hardware.lift.getCurrentPosition());
logger.addLogItem("x", () -> localizer.getPose().getX(DistanceUnit.CM));
return new ParallelRaceTask(myRoutine, logger);   // logger never finishes, so the race ends with myRoutine
```

`logger.addDrivetrainItems()` adds the columns used for [calibration](calibration.md). Files are
written to `/Android/data/com.qualcomm.ftcrobotcontroller/files/logs/` on the card.

## Writing your own task

A task that moves a servo and finishes straight away:

```java
@Ends
public class SetClaw extends Task {

    private final double position;

    public SetClaw(PeregrineOpMode opMode, double position) {
        this.opMode = opMode;   // inherited from Task
        this.position = position;
    }

    @Override
    public boolean run() {
        opMode.hardware.claw.setPosition(position);   // add `claw` to Hardware.java first
        return true;
    }

    @Override public void end() {}                    // nothing to clean up
    @Override public Task reset() { return new SetClaw(opMode, position); }
}
```

A task that runs an intake for a number of seconds:

```java
@Ends
public class RunIntake extends Task {

    private final double seconds;
    private ElapsedTime timer;

    public RunIntake(PeregrineOpMode opMode, double seconds) {
        this.opMode = opMode;
        this.seconds = seconds;
    }

    @Override
    public boolean run() {
        // Start the timer on the first run, not in the constructor: tasks are built during init.
        if (timer == null) timer = new ElapsedTime();
        if (timer.seconds() >= seconds) {
            opMode.hardware.intake.setPower(0);
            return true;
        }
        opMode.hardware.intake.setPower(1);
        return false;
    }

    @Override
    public void end() {
        opMode.hardware.intake.setPower(0);          // stopped early: turn the motor off
    }

    @Override
    public Task reset() {
        return new RunIntake(opMode, seconds);
    }
}
```

## Things to know

- **Constructors run during init.** Start timers and read sensors in `run()`, not in the
  constructor.
- **In a `SeriesTask`, the next task starts on the same loop the previous one finished.**
- **In a `ParallelTask`, a child that has finished isn't run again** while the others carry on.
- **A `ParallelRaceTask` ends the losers.** When one child finishes, `end()` is called on every child
  that hasn't. A race given a single task behaves exactly like that task: it finishes when the task
  does, and it only counts as ending (`ends()`) if the task does.
- **Don't put the same task object in a tree twice.** Each task keeps its own progress (such as a
  timer). If you need the same action twice, create two tasks, or use `task.reset()` to get a fresh
  copy.
- **When the whole tree finishes, the opMode ends.** See
  [When does the opMode end?](opmodes.md#when-does-the-opmode-end)
