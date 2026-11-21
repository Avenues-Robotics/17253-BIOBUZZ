---
title: Common mistakes and glossary
parent: Learn Peregrine
nav_order: 7
---

# Common mistakes and glossary
{: .no_toc }

1. TOC
{:toc}

## Common mistakes

| What you see | Likely cause | Fix |
| --- | --- | --- |
| The whole robot freezes; odometry stops updating. | A task uses `sleep()` or a long `while` loop inside `run()`. | Return quickly from `run()`. Use `WaitTask` or `WaitUntilTask` to wait. |
| A timer-based task finishes instantly or too early. | Its timer was started in the constructor, which runs at INIT. | Start the timer on the first `run()`: `if (timer == null) timer = new ElapsedTime();` |
| `NullPointerException` the first time a task runs. | The constructor is missing `this.opMode = opMode;`, or a device is missing from `Hardware.java`. | Set `opMode` in every task constructor. Check the device is declared *and* mapped in `Hardware`. |
| A `SeriesTask` gets stuck on one step forever. | That task's `run()` never returns `true`. | Check the condition that should end it. |
| The opMode ends right after START. | The task tree finished immediately. | A teleop needs something that never finishes, such as `TeleopMovement`. |
| The opMode stops at INIT with `No SD Card found.` or `No tables found on SD Card` | The opMode loads the pathing tables by default. | If it never uses `Drive`, override `buildOptimalityEngine()` to return `false`. |
| `Invalid task, for togglable TeleopTasks ensure the selected task can end.` | A press-to-start `TeleopTask` was given a task without `@Ends`. | Add `@Ends` if the task always finishes, or use hold mode (`false`). |
| The second use of a task does nothing. | The same task **object** was used twice. | Create a new object for each use. |
| Error at INIT mentioning a device name. | The name in `hardwareMap.get(…, "name")` doesn't match the Driver Station configuration. | Make them match exactly, including capitals. |
| Red text under a class name in Android Studio. | A missing `import`, or unimplemented abstract methods. | Click it, press **Alt+Enter** (**Option+Enter** on a Mac), and pick the fix. |
| A lambda's value never changes. | The value was copied into a variable before the lambda, instead of read inside it. | Read it inside the lambda: `() -> gamepad1.a`, not `boolean a = gamepad1.a; () -> a`. |

For errors from pathing and the SD card, see [Troubleshooting](../troubleshooting.md).

## Glossary

| Word | Meaning |
| --- | --- |
| **Task** | One thing the robot does, as an object with `run()`, `end()` and `reset()`. |
| **Loop** (or tick) | One pass of Peregrine's main cycle, in which every active task's `run()` is called once. |
| **Task tree** | Tasks nested inside combiners such as `SeriesTask`. The top one is the **root**. |
| **Finish** | A task finishes when its `run()` returns `true`. |
| **End** | Stopping a task *before* it finishes, by calling its `end()`. |
| **opMode** | A program you choose on the Driver Station. In Peregrine, a class that extends `PeregrineOpMode`. |
| **INIT / START / STOP** | The Driver Station buttons. Tasks are built at INIT and run after START. |
| **Library** | Code written for other code to build on. Peregrine is one. |
| **Class / object** | A blueprint, and one thing built from it with `new`. |
| **Subclass / superclass** | A class that `extends` another, and the one it extends. |
| **Abstract method** | A method with no body that every subclass must fill in. |
| **Lambda** | A small piece of code passed as a value, like `() -> gamepad1.a`. It runs later, when it's called. |
| **Annotation** | A label starting with `@`, such as `@TeleOp` or `@Ends`. |
| **Pose** | Where the robot is and which way it faces: x, y and heading. |
| **Field frame** | Coordinates measured from the field, rather than from the robot. |
| **Localizer** | The part of Peregrine that tracks the robot's pose using the goBILDA Pinpoint. |
| **Target** | A named place on the field that `Drive` can go to, such as `"basket"`. |
| **Tables** | Files on the SD card, made on a desktop computer, that tell `Drive` the fastest way to each target. |
| **Telemetry** | Text shown on the Driver Station and FTC Dashboard while an opMode runs. |
| **FTC Dashboard** | A web page (http://192.168.43.1:8080/dash) for telemetry and live-editing values. |

## Where next

You now know enough to write real opModes. The reference pages go into more detail:

- [Writing opModes](../opmodes.md): every method, and exactly what happens when.
- [Tasks](../tasks.md): every built-in task, logging, and more examples.
- [Driving to targets](../driving-to-targets.md): using `Drive` and the pathing tables.
- [Tuning with Dashboard](../tuning.md): changing values while the robot runs.
- [QA testing](../qa-testing.md): writing robot check-lists.
