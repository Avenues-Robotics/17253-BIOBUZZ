---
title: Tuning with Dashboard
nav_order: 10
---

# Tuning with FTC Dashboard
{: .no_toc }

1. TOC
{:toc}

Connect to the robot's Wi-Fi and open **http://192.168.43.1:8080/dash** in a browser. You'll see
telemetry from `opMode.telem`, plus a **Configuration** panel where the values below can be edited
live. Any `public static` field in a class marked `@Config` shows up there.

{: .note }
Dashboard edits are lost when the app restarts. Once you've found a value you like, copy it into
the code.

## Driving to targets (`Drive`)

| Parameter | Default | Meaning |
| --- | --- | --- |
| `PIDDist` | 3 cm | Hand over to the honing PID within this distance of the target… |
| `PIDAng` | 0.5 rad | …and within this heading error. |
| `DoneDist` | 0.5 cm | `Drive` finishes within this distance… |
| `DoneAng` | 0.08 rad | …and within this heading error. |

Takes effect immediately.

## The optimizer (`OptimalityEngine`)

| Parameter | Default | Meaning |
| --- | --- | --- |
| `deadZone` | 0.3 | Ignore any drive axis (forward, strafe or turn) that contributes less than this fraction of the strongest one. Higher values give simpler, less twitchy commands. Lower values give more exact ones. |
| `window` | 4 | How many recent commands are averaged to smooth out chatter. Higher is smoother but slower to react. Read once per opMode, the first time it drives to a target. |
| `iterations` | 6 | Solver iterations per loop. You shouldn't need to change this. |

## Robot constants (`RobotParams`)

Chassis type, odometry offsets and directions, and motor directions. See
[Getting started](getting-started.md#robotparams). These are only read when an opMode is
initialised.

## QA runs (`QAConfig`)

| Parameter | Default | Meaning |
| --- | --- | --- |
| `tester` | `Unnamed Tester` | Who is running the tests. Written at the top of the log. |
| `logName` | `Unnamed Log` | The log's file name. **Change this** before each run; a QA routine won't start with the default. |

See [QA testing](qa-testing.md).

## Your own values

Mark any class `@Config` and its `public static` fields become editable too. This is handy for servo
positions and motor powers while you're still finding the right numbers:

```java
@Config
public class ClawPositions {
    public static double OPEN = 1.0;
    public static double CLOSED = 0.2;
}
```

Read them where you use them (`ClawPositions.OPEN`), not once in a constructor, so that edits take
effect straight away.

## What isn't tunable

The **honing PID gains** come from `MODEL.JSON` and are derived from the same drivetrain fit as
everything else. If the robot's final approach is off, re-calibrate rather than tweaking gains.
