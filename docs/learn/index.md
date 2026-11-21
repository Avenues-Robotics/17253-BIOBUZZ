---
title: Learn Peregrine
nav_order: 2
has_children: true
---

# Learn Peregrine

A step-by-step introduction for people who have never used Peregrine, and who are still getting
comfortable with classes and objects in Java.
{: .fs-5 .fw-300 }

## Who this is for

You should already know the Java basics: variables, `if` statements, loops, and methods. You don't
need to be confident with **object-oriented programming** (classes, objects, `extends`, and so on).
Peregrine uses those ideas everywhere, so the first page explains each one using Peregrine's own
code as the example.

## What Peregrine is

Peregrine is a **library**: code someone else wrote that your code builds on. Without it, an FTC
opMode is usually one long method full of `sleep()` calls, where the robot can only do one thing at a
time. With Peregrine, you describe what the robot should do as small pieces called **tasks**, such as
"close the claw", "wait half a second" or "drive to the basket". You then snap those pieces together,
and Peregrine runs them, many at once if you like.

It also keeps track of where the robot is on the field, and it can drive the robot to named places as
fast as possible.

## Before you start

- Open the team's project in **Android Studio**, and make sure you can build it and deploy it to the
  robot (plug into the Control Hub or connect over Wi-Fi, then press the green **Run** arrow).
- The robot's configuration on the Driver Station must include the goBILDA Pinpoint as `odo` and the
  four drive motors as `FR`, `FL`, `BR` and `BL`. Ask whoever set up the robot if you're not sure.
- You don't need the SD card with the pathing tables until the very end of this tutorial.

## Where things are in the code

All paths are inside `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.

| Folder | What's in it | Will you edit it? |
| --- | --- | --- |
| `peregrine/core/` | The library itself: the base classes, the built-in tasks, pathing. | Rarely. Read it to see how things work. |
| `peregrine/editables/` | `Hardware.java` (motors and sensors), `RobotParams.java` (robot measurements), `GlobalVariables.java` (shared values). | **Yes**, whenever the robot changes. |
| anywhere else in `teamcode/` | Your own opModes and tasks. | **Yes.** This is where you'll work. |

## The pages

Work through them in order. Each one builds on the last, and most end with a few exercises. Try
them on the robot before looking at the answers.

1. [Java ideas Peregrine uses](java-concepts.md): classes, inheritance, abstract methods, lambdas.
2. [How Peregrine runs](how-it-runs.md): the loop, tasks, and why you never use `sleep()`.
3. [Your first opMode](first-opmode.md): drive the robot around.
4. [Your first task](first-task.md): make a mechanism move.
5. [Combining tasks](combining-tasks.md): sequences, things at the same time, and time limits.
6. [Teleop controls](teleop-controls.md): put tasks on gamepad buttons.
7. [Common mistakes and glossary](common-mistakes.md): what goes wrong, and what the words mean.

When you've finished, the reference pages ([Writing opModes](../opmodes.md), [Tasks](../tasks.md),
[Driving to targets](../driving-to-targets.md), and so on) have the full details.
