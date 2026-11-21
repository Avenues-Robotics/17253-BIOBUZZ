---
title: Calibration
nav_order: 8
---

# Calibration
{: .no_toc }

1. TOC
{:toc}

Pathing depends on a model of how **your** robot accelerates, turns and slips. You build that model
by driving the robot around while it logs everything, then fitting the log on the desktop.

## Before you start

- Set up and check `RobotParams` first, **especially the odometry offsets**. See
  [Getting started](getting-started.md#robotparams).
- Insert an SD card. Calibration only writes logs to it, so it doesn't need any tables yet.
- Calibrate on the **surface you'll compete on**. Grip varies a lot between floors, and a model fitted
  on a slippery practice floor makes the robot look weaker than it is.

## Running it

1. Place the robot anywhere with room to drive. Calibration treats wherever it starts as (0, 0),
   heading 0.
2. On the Driver Station, select the **Calibration** teleop, then INIT and START.
3. Drive with gamepad 1:

   | Control | Action |
   | --- | --- |
   | Left stick | Translate (forward/back and strafe) |
   | Right stick, x axis | Turn |

   Stick input is cubed (keeping its sign) for finer control near centre.
4. Press STOP when you're done. Each run writes a new log file.

## How to drive

The fit can only learn behaviour you actually show it. Aim to cover:

- every direction: forward, back, strafing both ways, diagonals;
- turning on the spot, and turning **while** translating;
- hard, full-stick starts and stops;
- some runs that push past the point where the wheels start to slip, so the traction limit gets
  measured;
- a range of speeds, not just full throttle.

A few minutes of varied driving is a good start.

## Where the logs go

On the SD card:

```
/Android/data/com.qualcomm.ftcrobotcontroller/files/logs/log_YYYYMMDD_HHMMSS.csv
```

To get them off, power down and read the card on a computer, or use `adb`:

```bash
adb shell ls /storage
```

```bash
adb pull /storage/XXXX-XXXX/Android/data/com.qualcomm.ftcrobotcontroller/files/logs
```

(Replace `XXXX-XXXX` with the card's ID from the first command.)

Each row of the CSV is one loop:

| Column | Meaning | Unit |
| --- | --- | --- |
| `timestamp` | Time since the logger started | ms |
| `FR`, `FL`, `BR`, `BL` | Motor power | −1…1 |
| `x`, `y` | Position | cm |
| `h` | Heading | rad |
| `x_vel`, `y_vel` | Velocity (field frame) | cm/s |
| `h_vel` | Angular velocity | rad/s |

## Next

Feed the logs to the desktop fitter (`calibration/fit_drivetrain.py` in `peregrine-desktop`), then
solve the tables and copy the output to the [SD card](sd-card.md).

Re-calibrate whenever the drivetrain or the driving surface changes.
