---
title: Your first task
parent: Learn Peregrine
nav_order: 4
---

# Your first task
{: .no_toc }

You'll add a mechanism to `Hardware.java`, then write tasks that move it: one that finishes
instantly, one that takes time, and one that never finishes.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

The examples use a **claw servo** called `claw` and an **intake motor** called `intake`. Use whatever
your robot really has, with the names from the Driver Station configuration.

## 1. Add the hardware

Open `peregrine/editables/Hardware.java`. It has a field for each device, and a constructor that
finds each one in the configuration. Add yours in both places:

```java
import com.qualcomm.robotcore.hardware.Servo;

public class Hardware {

    // ...the existing fields...
    public Servo claw;
    public DcMotor intake;

    public Hardware(PeregrineOpMode opMode) {
        // ...the existing code...

        claw = opMode.hardwareMap.get(Servo.class, "claw");
        intake = opMode.hardwareMap.get(DcMotor.class, "intake");
    }
}
```

The name in quotes must match the Driver Station configuration exactly. Now any task can reach the
claw as `opMode.hardware.claw`, and any opMode as just `hardware.claw`.

## 2. A task that finishes instantly

Create a new class `SetClaw` next to your opMode:

```java
package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.peregrine.core.annotations.Ends;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

@Ends                                   // this task always finishes
public class SetClaw extends Task {

    private final double position;      // where to move the claw, 0 to 1

    public SetClaw(PeregrineOpMode opMode, double position) {
        this.opMode = opMode;           // the opMode field comes from Task
        this.position = position;
    }

    @Override
    public boolean run() {
        opMode.hardware.claw.setPosition(position);
        return true;                    // finished after one loop
    }

    @Override
    public void end() {
        // Nothing to undo.
    }

    @Override
    public Task reset() {
        return new SetClaw(opMode, position);
    }
}
```

Piece by piece:

- **`extends Task`** makes it a task, so it can go anywhere a task can.
- **The fields and constructor** remember *which* position this particular claw task is for.
  `new SetClaw(this, 0)` and `new SetClaw(this, 1)` are two different objects from the same class.
- **`this.opMode = opMode;`** is required in every task constructor. Forget it, and `opMode` is `null`
  and the task crashes with a `NullPointerException` the first time it runs.
- **`run()`** sets the servo and returns `true`: done.
- **`end()`** is empty because there's nothing to make safe.
- **`reset()`** returns a fresh copy built with the same inputs.
- **`@Ends`** tells Peregrine this task is guaranteed to finish. You'll see why that matters on the
  [Teleop controls](teleop-controls.md) page.

## 3. Try it in an autonomous

Make a new opMode, `ClawTest`, the same way as on the last page, but with `@Autonomous` instead of
`@TeleOp`:

```java
@Autonomous(name = "Claw Test")
public class ClawTest extends PeregrineOpMode {

    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    @Override
    public Task defineTasks() {
        return new SeriesTask(
                new SetClaw(this, 0),        // close
                new WaitTask(this, 1000),    // wait 1 second
                new SetClaw(this, 1)         // open
        );
    }

    @Override
    protected boolean buildOptimalityEngine() {
        return false;
    }

    @Override public void initStart() {}
    @Override public void initLoop() {}
    @Override public void mainStart() {}
    @Override public void mainLoop() {}
    @Override public void end() {}
}
```

Press INIT and START. The claw closes, waits a second, opens, and then the opMode ends by itself,
because the tree has finished.

## 4. A task that takes time

Running the intake for a few seconds needs more than one loop. The task checks a timer each time
it runs:

```java
@Ends
public class RunIntake extends Task {

    private final double seconds;
    private ElapsedTime timer;          // null until the first run()

    public RunIntake(PeregrineOpMode opMode, double seconds) {
        this.opMode = opMode;
        this.seconds = seconds;
    }

    @Override
    public boolean run() {
        // Start the timer the first time we run, not in the constructor.
        if (timer == null) timer = new ElapsedTime();

        if (timer.seconds() >= seconds) {
            opMode.hardware.intake.setPower(0);
            return true;                // time's up: finished
        }
        opMode.hardware.intake.setPower(1);
        return false;                   // keep going next loop
    }

    @Override
    public void end() {
        opMode.hardware.intake.setPower(0);   // stopped early: turn it off
    }

    @Override
    public Task reset() {
        return new RunIntake(opMode, seconds);
    }
}
```

{: .warning }
Why not start the timer in the constructor? Because tasks are built in `defineTasks()`, during
**INIT**. If the robot sits in INIT for 20 seconds, a timer started then has already run out by the
time the task actually starts.

This task needs a real `end()`. If it's stopped halfway, for example because it lost a race (next
page), `end()` makes sure the intake doesn't keep spinning.

## 5. A task that never finishes

Some tasks should just keep going until something else stops them:

```java
public class SpinIntake extends Task {      // no @Ends: it never finishes

    public SpinIntake(PeregrineOpMode opMode) {
        this.opMode = opMode;
    }

    @Override
    public boolean run() {
        opMode.hardware.intake.setPower(1);
        return false;                       // never finished
    }

    @Override
    public void end() {
        opMode.hardware.intake.setPower(0);
    }

    @Override
    public Task reset() {
        return new SpinIntake(opMode);
    }
}
```

On its own, this would spin forever. It becomes useful when it's combined with other tasks, or bound
to a button, which is what the next two pages cover.

## Why `reset()`?

A task remembers its progress: `RunIntake` keeps its timer, and once it has returned `true`, it's
done. If you want to run the same action again, such as every time a button is pressed, you need a
fresh copy with a new timer. That's what `reset()` returns. Peregrine calls it for you when it needs
one.

## Try it

1. Change `ClawTest` so the claw opens and closes **three** times.
2. Write a task `SetIntakePower` that sets the intake to a given power and finishes immediately.
3. Add a `double power` parameter to `RunIntake`, so you can run the intake backwards
   (`new RunIntake(this, 2, -1)`).

<details markdown="block">
<summary>Answers</summary>

1. Use six claw tasks with waits between them. Each one must be a **new** object:

   ```java
   return new SeriesTask(
           new SetClaw(this, 0), new WaitTask(this, 500),
           new SetClaw(this, 1), new WaitTask(this, 500),
           new SetClaw(this, 0), new WaitTask(this, 500),
           new SetClaw(this, 1), new WaitTask(this, 500),
           new SetClaw(this, 0), new WaitTask(this, 500),
           new SetClaw(this, 1)
   );
   ```

2. The same shape as `SetClaw`:

   ```java
   @Ends
   public class SetIntakePower extends Task {
       private final double power;

       public SetIntakePower(PeregrineOpMode opMode, double power) {
           this.opMode = opMode;
           this.power = power;
       }

       @Override
       public boolean run() {
           opMode.hardware.intake.setPower(power);
           return true;
       }

       @Override public void end() {}
       @Override public Task reset() { return new SetIntakePower(opMode, power); }
   }
   ```

3. Add a field and a constructor parameter, use it in `run()`, and pass it along in `reset()`:

   ```java
   private final double power;

   public RunIntake(PeregrineOpMode opMode, double seconds, double power) {
       this.opMode = opMode;
       this.seconds = seconds;
       this.power = power;
   }

   // in run():   opMode.hardware.intake.setPower(power);
   // in reset(): return new RunIntake(opMode, seconds, power);
   ```

</details>

Next: [Combining tasks](combining-tasks.md).
