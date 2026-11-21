---
title: Home
layout: home
nav_order: 1
---

# Peregrine

An all-in-one FTC library for action scheduling, pathing, localization, and more.

{: .tip }
**New to Peregrine?** Start with [Learn Peregrine](learn/index.md). It walks you through the Java ideas
Peregrine is built on and has you writing tasks and opModes step by step.

{: .warning }
Peregrine is early and changing fast. Class names, file formats and workflows described here
will change. If these pages and the code disagree, trust the code.

## What it gives you

- **Tasks.** A small scheduling system. Every action is a `Task` that is run once per loop, and
  tasks can be combined to run one after another, all at once, or as a race. Ready-made tasks cover
  waiting, one-off actions, and binding tasks to gamepad buttons.
- **Localization.** goBILDA Pinpoint odometry, updated automatically every loop.
- **Minimum-time pathing.** Drive to named targets using value tables that are computed ahead of
  time on a desktop and stored on the Control Hub's SD card. A PID takes over for the last few
  centimetres.
- **Calibration.** A teleop that logs how your drivetrain actually moves, so the desktop tools can
  fit a model of it.
- **QA testing.** A framework for writing robot check-lists that log PASS/FAIL results to a file.

## How the pieces fit together

Peregrine is split into two halves:

| Where | What it does |
| --- | --- |
| **Desktop** (the separate `peregrine-desktop` project) | Fits a drivetrain model from calibration logs, then solves a value table for every target and writes both to an SD card. |
| **Robot** (this repository) | Reads odometry, looks the robot's current state up in those tables, and turns the result into motor powers. |

All of the heavy searching happens on the desktop. The robot only does a handful of table reads and
some small matrix maths each loop.

## Typical workflow

1. [Install Peregrine](getting-started.md) into your FTC project and set up your hardware.
2. [Run the Calibration teleop](calibration.md) to record how your robot drives.
3. On the desktop, fit the model, solve the tables, and [copy them to the SD card](sd-card.md).
4. [Write an opMode](opmodes.md) out of [tasks](tasks.md), using `Drive` to
   [go to targets](driving-to-targets.md).
5. [Tune](tuning.md) from FTC Dashboard, [check the robot](qa-testing.md) with a QA routine, and
   [troubleshoot](troubleshooting.md) when things go sideways.

## Current limitations

- Only **mecanum** drivetrains are implemented.
- Only the **goBILDA Pinpoint** is supported for localization, and every opMode needs one.
- By default, every opMode loads the pathing tables at INIT and needs an SD card with
  `MANIFEST.JSON` and `MODEL.JSON` on it. opModes that never drive to targets can
  [opt out](opmodes.md#opting-out-of-the-sd-card-tables).
