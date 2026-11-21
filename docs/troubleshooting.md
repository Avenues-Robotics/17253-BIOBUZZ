---
title: Troubleshooting
nav_order: 11
---

# Troubleshooting
{: .no_toc }

1. TOC
{:toc}

## Error messages

Most setup problems stop the opMode during INIT and show an error message on the Driver Station.

### Loading the tables (at INIT, unless the opMode opted out)

| Message | What it means | Fix |
| --- | --- | --- |
| `No SD Card found.` | The Control Hub can't see a removable card. | Check the card is inserted, then power-cycle. |
| `SD Card not mounted, did you format it to FAT32?` | The card is there but unreadable. | Reformat as FAT32 and copy the tables again. |
| `No tables found on SD Card` | `MANIFEST.JSON` or `MODEL.JSON` is missing from the card root. | Copy the solver output to the **root** of the card, not into a folder. If this opMode doesn't drive to targets, [opt out](opmodes.md#opting-out-of-the-sd-card-tables) of the tables instead. |
| `Exception while reading JSONs from SD Card` | A JSON file couldn't be parsed. | Re-copy the files; run `verify_tables.py`. |
| `Tables must be generated in field frame` | The tables were solved in the wrong frame. | Re-solve in the field frame. |
| `SD card uses unsupported datatype …` / `SD card table datatype … should be N bytes` | The manifest's encoding is invalid. | Re-solve with `u16` (the default). |
| `Could not find table binary on SD card at …` | A `TABLES/*.BIN` chunk is missing. | Re-copy the `TABLES` folder; run `verify_tables.py`. |
| `Unexpected error while finding SD Card` | Android wouldn't list the storage volumes. | Restart the robot. |

### Building tasks (at INIT)

| Message | What it means | Fix |
| --- | --- | --- |
| `Invalid target name, no such target present on SD Card.` | A `Drive` or `PIDHold` target name isn't in `MANIFEST.JSON`. | Names must match exactly, including capitals. |
| `This opMode's buildOptimalityEngine() returns false, but Drive needs the OptimalityEngine.` | The opMode opted out of the tables but uses `Drive` (or `PIDHold`). | Remove the `buildOptimalityEngine()` override. |
| `Invalid task, for togglable TeleopTasks ensure the selected task can end.` | A `TeleopTask` with `togglable = true` was given a task that never finishes. | Add `@Ends` to the task if it does always finish, or use `togglable = false`. |
| `New log items can only be added before logger is run` | `addLogItem()` was called after the `Logger` started. | Add all columns when you build the `Logger`, in `defineTasks()`. |

### While driving

| Message | What it means | Fix |
| --- | --- | --- |
| `A value from the pathing table could not be retrieved` (telemetry) | A read from the card failed mid-run. | Check the card is seated. Try another card. |

### Logging (`Logger`, including Calibration)

| Message | What it means | Fix |
| --- | --- | --- |
| `No removable storage found.` | No SD card found. | Insert the card and power-cycle. |
| `Card not mounted, ensure it's formatted to FAT32` | The card can't be read. | Reformat the card as FAT32. |
| `Log directory couldn't be created` | The logs folder couldn't be made on a mounted card. | The card may be full or write-protected. |
| `unexpected error while writing log` | Writing the log failed. | The card may be full or have been removed. |

### QA routines

| Message | What it means | Fix |
| --- | --- | --- |
| `Rename the log to something else!` | `QAConfig.logName` is still `Unnamed Log`. | Set it in FTC Dashboard's Configuration panel before INIT. |

### Odometry (at INIT)

| Symptom | What it means | Fix |
| --- | --- | --- |
| `Odo Status` and `Loop Number` keep counting up and init never finishes | The Pinpoint never reported ready. | Check the wiring and that it's named `odo` in the configuration, then restart the opMode. This is a known intermittent issue. |

## Behaviour

**The opMode ends by itself right after START.**
The task tree from `defineTasks()` finished. An opMode ends as soon as its tree does, so a teleop
needs at least one task that never finishes, such as `TeleopMovement`. See
[When does the opMode end?](opmodes.md#when-does-the-opmode-end)

**The robot freezes, and odometry stops updating.**
A task is blocking the loop, for example with `sleep()` or a `while` loop inside `run()`. Every task
must return quickly. Use `WaitTask` or `WaitUntilTask` to wait.

**A timer in my task finishes instantly, or too early.**
The timer was started in the constructor, which runs during INIT. Start it on the task's first
`run()` instead.

**The robot creeps slowly toward the target instead of driving fast.**
It is in the PID fallback because the tables have nothing useful at its current state. Usually the
`startingPose()` is wrong, or it's outside the table area (the tables are inset from the walls by
about the robot's footprint). Double-check the starting pose and units: centimetres and radians.

**The robot drives confidently in the wrong direction, or spins.**
Something disagrees about which way is which. In this order, check:
1. the motor directions in `RobotParams`;
2. the odometry encoder directions;
3. the odometry offsets;
4. the starting heading.

**The robot overshoots or oscillates during the final approach.**
The drivetrain model probably doesn't match the robot any more, for example after a new floor or
different wheels. [Re-calibrate](calibration.md) and re-solve.

**The robot twitches between directions while driving.**
Try raising `deadZone` or `window` in [Dashboard](tuning.md).
