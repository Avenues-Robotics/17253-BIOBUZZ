---
title: Writing opModes
nav_order: 4
---

# Writing opModes
{: .no_toc }

1. TOC
{:toc}

Peregrine replaces `LinearOpMode` with one base class, `PeregrineOpMode`. Every autonomous and teleop
extends it directly and adds the usual `@Autonomous` or `@TeleOp` annotation.

## A minimal autonomous

```java
@Autonomous
public class MyAuto extends PeregrineOpMode {

    // Where the robot is when you press INIT. Field frame, same as the tables.
    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 60, 60, AngleUnit.RADIANS, 0);
    }

    // The whole routine, as one task tree. The opMode ends when it finishes.
    @Override
    public Task defineTasks() {
        return new SeriesTask(
                new Drive(this, "score_left"),
                new Drive(this, "park")
        );
    }

    @Override public void initStart() {}
    @Override public void initLoop() {}
    @Override public void mainStart() {}
    @Override public void mainLoop() {}
    @Override public void end() {}
}
```

## A minimal teleop

```java
@TeleOp
public class MyTeleop extends PeregrineOpMode {

    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    // TeleopMovement never finishes, so the opMode runs until you press STOP.
    @Override
    public Task defineTasks() {
        return new TeleopMovement(this);
    }

    // This teleop never drives to a target, so it doesn't need the SD card tables.
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

To add buttons for your mechanisms, see [`TeleopTask`](tasks.md#teleop).

## The methods you implement

| Method | When it runs | Use it for |
| --- | --- | --- |
| `startingPose()` | Once, at INIT | Returning where the robot starts. **Accuracy matters**, because everything after this is relative to it. |
| `defineTasks()` | Once, at INIT | Building and returning your task tree. |
| `initStart()` | Once, at INIT, right after `defineTasks()` | One-off setup, such as moving a servo to its starting position. |
| `initLoop()` | Repeatedly, until START | Anything you want to do while waiting, such as reading a camera. |
| `mainStart()` | Once, at START | One-off setup at the moment the match begins. |
| `mainLoop()` | Once per loop, after START | Extra telemetry. Put robot behaviour in tasks instead. |
| `end()` | Once, when the opMode finishes | Cleanup. |

All seven are required, so write an empty body `{}` for the ones you don't need. In Android Studio,
**Code → Implement Methods** (Ctrl+I) writes them all for you.

## Opting out of the SD card tables

By default, every opMode builds the `OptimalityEngine` at INIT, which loads the pathing tables from
the SD card and stops the opMode if they're missing. If an opMode never uses `Drive` or `PIDHold`,
override `buildOptimalityEngine()` to skip that:

```java
@Override
protected boolean buildOptimalityEngine() {
    return false;
}
```

`opMode.optimalityEngine` is then `null`, and building a `Drive` or `PIDHold` throws an error at INIT.

## What happens, in order

| Stage | What Peregrine does |
| --- | --- |
| **INIT pressed** | Sets up telemetry. Maps the hardware using `RobotParams` and resets the Pinpoint. Waits for the Pinpoint to report ready, then sets its pose to `startingPose()`. Loads the SD card tables (unless you opted out). Calls your `defineTasks()`, then `initStart()`. |
| **During init** | Calls `initLoop()` repeatedly. |
| **START pressed** | Calls `mainStart()`. |
| **Every loop** | Updates the localizer, runs your task tree once, calls `mainLoop()`, then sends telemetry. |
| **Task tree finishes, or STOP pressed** | Closes the table files, then calls your `end()`. |

{: .warning }
`defineTasks()` runs during **init**, not at start, so your tasks' constructors run then too.
Anything that should begin at START, such as a timer, belongs in the task's first `run()`. See
[Tasks](tasks.md).

## What tasks can reach through `opMode`

Every task gets the opMode it belongs to, and through it:

| Field | What it is |
| --- | --- |
| `opMode.hardware` | Your motors and sensors, from `editables/Hardware.java`. |
| `opMode.localizer` | Current pose and velocity. See `getPose()`, `getVelX()`, `getVelY()`, `getHeadingVelocity()`, `getStateVector()`. |
| `opMode.optimalityEngine` | The pathing tables. You normally only use this through `Drive`. `null` if the opMode opted out. |
| `opMode.globalVariables` | Your own shared state, from `editables/GlobalVariables.java`. |
| `opMode.telem` | Telemetry to both the Driver Station and FTC Dashboard. It is updated once per loop for you. |
| `opMode.gamepad1`, `opMode.gamepad2` | The usual gamepads. |

Inside the opMode class itself, the same things are available without the `opMode.` prefix, for
example `hardware.claw` or `gamepad1.a`.

## When does the opMode end?

When the task tree from `defineTasks()` finishes, or when you press STOP, whichever comes first. An
autonomous made of tasks that all finish (such as a `SeriesTask` of `Drive`s) ends by itself once
the routine is done. A teleop built from tasks that never finish, such as `TeleopMovement`, runs
until you press STOP.
