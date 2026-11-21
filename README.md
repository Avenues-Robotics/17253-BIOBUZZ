# Peregrine
An all-in-one FTC library for action scheduling, pathing, localization, and more.

**Documentation:** https://noah-mg.github.io/Peregrine/ (built from the [`docs`](docs) folder).
New to Peregrine? Start with [Learn Peregrine](docs/learn/index.md).

## What it does

- **Tasks.** Every robot action is a small `Task` that runs a little each loop. Combine tasks to run
  one after another (`SeriesTask`), at the same time (`ParallelTask`), or as a race
  (`ParallelRaceTask`), and bind them to gamepad buttons with `TeleopTask`. Nothing blocks, so the
  robot can do many things at once.
- **Localization.** goBILDA Pinpoint odometry, updated automatically every loop.
- **Minimum-time pathing.** `Drive` takes the robot to named targets as fast as its drivetrain allows,
  using value tables solved ahead of time on a desktop (the separate `peregrine-desktop` project) and
  stored on the Control Hub's SD card. A PID takes over for the last few centimetres.
- **Calibration.** A teleop that logs how your drivetrain really moves, so the desktop tools can fit a
  model of it.
- **QA testing.** Write robot check-lists that log PASS/FAIL results to a file.

## A taste

```java
@Autonomous
public class MyAuto extends PeregrineOpMode {

    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 60, 60, AngleUnit.RADIANS, 0);
    }

    @Override
    public Task defineTasks() {
        return new SeriesTask(
                new InstantTask(() -> hardware.claw.setPosition(0)),
                new Drive(this, "basket"),
                new InstantTask(() -> hardware.claw.setPosition(1)),
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

## Where things are

| Path | What's in it |
| --- | --- |
| `TeamCode/.../teamcode/peregrine/core/` | The library: `PeregrineOpMode`, the built-in tasks, pathing, calibration. |
| `TeamCode/.../teamcode/peregrine/editables/` | Your robot's `Hardware`, `RobotParams` and `GlobalVariables`. |
| `TeamCode/.../teamcode/qa/` | The QA testing framework. |
| `docs/` | The documentation site. |

## Requirements

FTC SDK 11.1, a REV Control Hub, a mecanum drivetrain, a goBILDA Pinpoint, and (for pathing) a FAT32
microSD card. See [Getting started](docs/getting-started.md) for installation.

## Status

Peregrine is early and changing fast. If the docs and the code disagree, trust the code.
