---
title: How Peregrine runs
parent: Learn Peregrine
nav_order: 2
---

# How Peregrine runs
{: .no_toc }

Before writing any code, it helps to see the big idea: Peregrine runs everything in one fast loop,
a tiny slice at a time.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

## The problem with `sleep()`

In a plain FTC `LinearOpMode`, an autonomous often looks like this:

```java
claw.setPosition(0);
sleep(500);
lift.setPower(1);
sleep(1500);
lift.setPower(0);
```

It's easy to read, but while the program is inside `sleep()`, **nothing else happens**. The robot
can't drive while the lift rises, odometry doesn't update, and there's no way to react to a sensor
until the sleep is over.

## The loop

Peregrine works differently. After START, it repeats the same short cycle over and over, many times a
second. One pass through is called a **loop** (sometimes a **tick**). In every loop:

1. the odometry is updated, so Peregrine knows where the robot is;
2. **every active task gets a turn**: its `run()` method is called once;
3. telemetry is sent to the Driver Station.

Each task does a *tiny* bit of work in its turn, such as setting a motor power or checking a timer,
and hands control straight back. Because every turn is short, the loop comes round again almost
immediately, and all the tasks appear to run at the same time.

Think of a cook with several pots on the stove. They don't stand stirring one pot for ten minutes.
They give each pot a quick stir, move on to the next, and keep going round. Every pot gets attention
constantly.

{: .warning }
This only works if every task gives control back quickly. **Never use `sleep()` or a long `while`
loop inside a task.** One stuck task freezes the whole robot, odometry included.

## Tasks

A **task** is one thing the robot does. Every task is an object with three methods:

| Method | Peregrine calls it… | Your job in it |
| --- | --- | --- |
| `run()` | once every loop, while the task is active | Do a little work. Return `true` if the task is now **finished**, or `false` to be called again next loop. |
| `end()` | if the task is stopped before it finishes | Make the robot safe, e.g. turn a motor off. |
| `reset()` | when the task is needed again from the start | Return a brand-new copy of the task. |

So "raise the lift for 1.5 seconds" becomes a task whose `run()` says: *if 1.5 s have passed, turn
the motor off and return `true`; otherwise keep the motor on and return `false`.* It's called over and
over until it returns `true`.

## The task tree

Tasks can hold other tasks. A `SeriesTask` runs its tasks one after another; a `ParallelTask` runs
them all at once. Nest them and you get a **tree**:

```
SeriesTask
├── SetClaw(closed)
├── ParallelTask
│   ├── Drive("basket")
│   └── RaiseLift
└── SetClaw(open)
```

Read it top to bottom: close the claw; then drive to the basket **while** raising the lift; then
open the claw.

Your opMode builds this tree once and hands the top of it (the **root**) to Peregrine. Each loop,
Peregrine calls `run()` on the root, which calls `run()` on whichever of its children are active, and
so on down the tree.

## The life of an opMode

| When | What happens | Your code that runs |
| --- | --- | --- |
| **INIT** pressed | Peregrine sets up the hardware and odometry, and loads the SD card tables if needed. | `startingPose()`, then `defineTasks()`, then `initStart()` |
| Waiting for START | | `initLoop()`, over and over |
| **START** pressed | | `mainStart()` |
| Every loop | Odometry updates, then your task tree gets its turn, then telemetry is sent. | your tasks' `run()`, then `mainLoop()` |
| The tree finishes, or **STOP** is pressed | The opMode ends. | `end()` |

Two things to notice:

- `defineTasks()` runs at **INIT**, not at START. So every task's constructor runs at INIT too. A
  task that needs to know "when did I start?" must find out in its first `run()`, not in its
  constructor.
- **When the tree finishes, the opMode ends.** An autonomous finishes once its routine is done. A
  teleop keeps going because driving (`TeleopMovement`) never finishes.

## What you write

Almost all your code falls into two kinds of class:

- **Tasks**: one class per action your robot can do (close the claw, run the intake, raise the lift).
  You write these once and reuse them everywhere.
- **opModes**: one class per program on the Driver Station. An opMode mostly just says where the
  robot starts and builds a tree out of tasks.

## Check yourself

1. A task's `run()` returned `false`. What happens next loop?
2. Why does putting `sleep(1000)` inside a task's `run()` stop the robot from driving?
3. Your autonomous tree is a `SeriesTask` of three tasks that all finish. What happens after the
   third one finishes?

<details markdown="block">
<summary>Answers</summary>

1. Peregrine calls its `run()` again.
2. Everything shares one loop. While that task sleeps, no other task gets its turn and odometry
   doesn't update.
3. The `SeriesTask` finishes, so the whole tree has finished, and the opMode ends.

</details>

Next: [Your first opMode](first-opmode.md).
