---
title: Combining tasks
parent: Learn Peregrine
nav_order: 5
---

# Combining tasks
{: .no_toc }

Small tasks become whole routines when you combine them. Peregrine has three ways to do it, plus a
few ready-made tasks that save you writing tiny classes.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

## One after another: `SeriesTask`

Runs its tasks in order. Each one starts on the same loop the previous one finishes. The
`SeriesTask` finishes when the last one does.

```java
new SeriesTask(
        new SetClaw(this, 0),
        new RunIntake(this, 2),
        new SetClaw(this, 1)
)
```

```
time ──────────────────────────────────►
SetClaw(0) ▌
RunIntake   ████████████
SetClaw(1)              ▌
```

## At the same time: `ParallelTask`

Runs all its tasks at once. It finishes when **all** of them have finished.

```java
new ParallelTask(
        new RunIntake(this, 2),
        new SetClaw(this, 1)
)
```

```
time ──────────────────────────────────►
RunIntake   ████████████
SetClaw(1)  ▌
            ╰── ParallelTask finishes here, when the slowest is done
```

## First one wins: `ParallelRaceTask`

Also runs all its tasks at once, but finishes as soon as **any one** of them finishes. Every task
that hasn't finished yet is then stopped, and its `end()` is called.

A race is perfect for time limits: "spin the intake until the sensor sees a game piece, but give up
after 3 seconds":

```java
new ParallelRaceTask(
        new SpinIntake(this),                                        // never finishes
        new WaitUntilTask(() -> hardware.pieceSensor.isPressed()),  // a touch sensor
        new WaitTask(this, 3000)
)
```

Whichever of the sensor or the 3-second timer happens first ends the race. `SpinIntake` never
finishes by itself, so it's stopped, and its `end()` turns the intake off.

## Ready-made helpers

You don't need to write a class for every little thing.

| Task | What it does |
| --- | --- |
| `new WaitTask(this, 500)` | Waits 500 ms (counted from when it starts), then finishes. |
| `new WaitUntilTask(() -> condition)` | Finishes on the first loop the condition is `true`. |
| `new InstantTask(() -> code)` | Runs one piece of code once, then finishes. |
| `new EmptyTask()` | Does nothing and finishes straight away. Handy as a placeholder. |
| `new IdleTask()` | Does nothing and never finishes. Put it in a `ParallelTask` to keep an opMode running after the rest is done. |

`InstantTask` and `WaitUntilTask` take a **lambda** (see [Java ideas](java-concepts.md#lambdas-passing-code-as-a-value)).
So `SetClaw` could have been written without a class at all:

```java
new InstantTask(() -> hardware.claw.setPosition(0))
```

A class is still better when you use the same action in lots of places, or when it has several
steps.

## Nesting

All three combiners accept any tasks, **including other combiners**, so you can build a routine of
any shape:

```java
@Override
public Task defineTasks() {
    return new SeriesTask(
            new SetClaw(this, 0),                     // grab the preloaded piece
            new ParallelTask(
                    new Drive(this, "basket"),        // drive there...
                    new RaiseLift(this)               // ...while raising the lift
            ),
            new SetClaw(this, 1),                     // drop it
            new WaitTask(this, 300),
            new ParallelRaceTask(
                    new Drive(this, "park"),
                    new WaitTask(this, 4000)          // don't take more than 4 s to park
            )
    );
}
```

The indentation mirrors the tree. When reading one, start from the inside: each combiner is just one
step of the combiner around it.

{: .note }
`Drive(this, "basket")` drives to a target named `basket` using the pathing tables on the SD card,
so an opMode that uses it must **not** override `buildOptimalityEngine()` to return `false`. See
[Driving to targets](../driving-to-targets.md). Until you have tables, practise with your own tasks
and waits.

## Don't reuse a task object

Each task object keeps its own progress. If you put the **same object** in a tree twice, the second
use finds it already finished:

```java
Task wait = new WaitTask(this, 500);
new SeriesTask(new SetClaw(this, 0), wait, new SetClaw(this, 1), wait);  // WRONG: second wait is instant
```

Build a new object for each use instead: `new WaitTask(this, 500)` each time.

## Try it

1. Write an autonomous that closes the claw, then runs the intake for 2 seconds **while** waiting
   1 second and opening the claw.
2. Put a 1.5-second time limit on `new RunIntake(this, 5)`.
3. Without writing a new class, make a task that sets the intake to half power, waits until
   gamepad 1's A button is pressed, then turns the intake off.

<details markdown="block">
<summary>Answers</summary>

1. ```java
   return new SeriesTask(
           new SetClaw(this, 0),
           new ParallelTask(
                   new RunIntake(this, 2),
                   new SeriesTask(new WaitTask(this, 1000), new SetClaw(this, 1))
           )
   );
   ```

2. ```java
   new ParallelRaceTask(new RunIntake(this, 5), new WaitTask(this, 1500))
   ```

   When the wait finishes first, `RunIntake`'s `end()` turns the intake off.

3. ```java
   new SeriesTask(
           new InstantTask(() -> hardware.intake.setPower(0.5)),
           new WaitUntilTask(() -> gamepad1.a),
           new InstantTask(() -> hardware.intake.setPower(0))
   )
   ```

</details>

Next: [Teleop controls](teleop-controls.md).
