---
title: Teleop controls
parent: Learn Peregrine
nav_order: 6
---

# Teleop controls
{: .no_toc }

In teleop, the drivers decide when things happen. `TeleopTask` connects a task to a gamepad button.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

## `TeleopTask`

```java
new TeleopTask(this, task, () -> button, togglable)
```

| Argument | What it is |
| --- | --- |
| `this` | Your opMode. |
| `task` | The task to run when the button is pressed. |
| `() -> button` | A lambda that says whether the button is down, e.g. `() -> gamepad1.a`. |
| `togglable` | How the button behaves; see below. |

There are two ways a button can behave:

| `togglable` | Behaviour | Good for |
| --- | --- | --- |
| `false` | **Hold.** The task runs only while the button is held down. Letting go stops it (its `end()` is called). | Spinning an intake while a bumper is held. |
| `true` | **Press.** One press starts the task, and it runs until it finishes on its own, even after you let go. | Moving a claw, or a whole scoring sequence. |

Either way, once the task finishes or is stopped, it's reset so the next press starts it fresh. A
`TeleopTask` itself never finishes.

{: .warning }
With `togglable = true`, the task has to be one that finishes, or it would run forever. Peregrine
checks this when the `TeleopTask` is built, using the `@Ends` annotation, and stops the opMode at INIT
with `Invalid task, for togglable TeleopTasks ensure the selected task can end.` if it can't be sure.
Add `@Ends` to your task class if it always finishes.

## A full teleop

Driving and every button are separate tasks, all running at once in a `ParallelTask`:

```java
@TeleOp(name = "Competition Teleop")
public class CompetitionTeleop extends PeregrineOpMode {

    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    @Override
    public Task defineTasks() {
        return new ParallelTask(
                // Drive with gamepad 1's sticks.
                new TeleopMovement(this),

                // Gamepad 2: A closes the claw, B opens it.
                new TeleopTask(this, new SetClaw(this, 0), () -> gamepad2.a, true),
                new TeleopTask(this, new SetClaw(this, 1), () -> gamepad2.b, true),

                // Gamepad 2: hold the right bumper to run the intake.
                new TeleopTask(this, new SpinIntake(this), () -> gamepad2.right_bumper, false),

                // Gamepad 2: X runs a whole sequence with one press.
                new TeleopTask(this, new SeriesTask(
                        new SetClaw(this, 1),
                        new WaitTask(this, 300),
                        new RunIntake(this, 1)
                ), () -> gamepad2.x, true)
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

Things to notice:

- **The whole teleop is one `ParallelTask`.** None of its tasks ever finish, so the `ParallelTask`
  never finishes, so the opMode keeps running until STOP.
- **Each button is its own task.** Adding a control is just adding a line.
- **The X button's sequence** is a `SeriesTask`. Because every task inside it has `@Ends`, Peregrine
  knows the whole sequence finishes, so it's allowed with `togglable = true`.
- **Any condition can be a button**, not just a single button: `() -> gamepad2.left_trigger > 0.5`
  or `() -> gamepad2.a && gamepad2.b` work too.

## Sharing information between tasks

Sometimes one task needs to know something another task found out, for example how many game pieces
the robot is holding. Put it in `peregrine/editables/GlobalVariables.java`:

```java
public class GlobalVariables {
    public int piecesHeld = 0;
}
```

Every task can then read and change it through its opMode:

```java
opMode.globalVariables.piecesHeld++;
```

There's one `GlobalVariables` object per opMode run, so it starts fresh every time you press INIT.

## Showing information

Add telemetry in `mainLoop()` (as on the [first opMode](first-opmode.md) page), or from inside a
task with `opMode.telem.addData("name", value)`. Peregrine sends it to both the Driver Station and
FTC Dashboard once per loop.

## Try it

1. Add a control: holding gamepad 2's left bumper runs the intake **backwards**. You'll need a new
   task.
2. Add a control: one press of gamepad 2's Y waits until a touch sensor is pressed, then closes the
   claw.
3. Why can't you write `new TeleopTask(this, new SpinIntake(this), () -> gamepad2.y, true)`?

<details markdown="block">
<summary>Answers</summary>

1. Copy `SpinIntake` as `ReverseIntake` with `setPower(-1)` in `run()`, then add:

   ```java
   new TeleopTask(this, new ReverseIntake(this), () -> gamepad2.left_bumper, false)
   ```

   Or give `SpinIntake` a `power` parameter, like the `RunIntake` exercise on the
   [first task](first-task.md) page.

2. `WaitUntilTask` and `SetClaw` both have `@Ends`, so the sequence finishes and `true` is allowed:

   ```java
   new TeleopTask(this, new SeriesTask(
           new WaitUntilTask(() -> hardware.touch.isPressed()),
           new SetClaw(this, 0)
   ), () -> gamepad2.y, true)
   ```

3. `SpinIntake` never finishes (it has no `@Ends`), so with `togglable = true` it would never stop.
   Peregrine refuses it at INIT. Use `false` (hold) instead.

</details>

Next: [Common mistakes and glossary](common-mistakes.md).
