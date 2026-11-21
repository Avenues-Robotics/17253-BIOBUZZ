---
title: Getting started
nav_order: 3
---

# Getting started
{: .no_toc }

This page is about adding Peregrine to an FTC project. If your team's project already has it and you
just want to learn to use it, go to [Learn Peregrine](learn/index.md) instead.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

## Requirements

- An FTC project on **SDK 11.1** (the version Peregrine is currently built against).
- A **REV Control Hub** with a microSD card slot.
- A **mecanum** drivetrain with four motors.
- A **goBILDA Pinpoint** odometry computer with two odometry pods.
- A microSD card, formatted **FAT32**, for the value tables and calibration logs.

## 1. Copy Peregrine into your project

Peregrine lives in a single package. Copy this folder from the repository:

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/peregrine/
```

to the same path in your own project's `TeamCode` module. Keep the package name
`org.firstinspires.ftc.teamcode.peregrine`, since every file refers to it.

Inside it:

| Package | What's in it | Should you edit it? |
| --- | --- | --- |
| `peregrine/editables` | `Hardware`, `RobotParams`, `GlobalVariables` | **Yes.** This is where your robot's details go. |
| `peregrine/core` | The library itself: the opMode base class, tasks, pathing, calibration | Normally no. |

If you also want the [QA testing](qa-testing.md) framework, copy the `teamcode/qa/` folder too.

Your own opModes can live anywhere in `TeamCode`.

## 2. Add the dependencies

Open `build.dependencies.gradle` in the root of your project and add the FTC Dashboard repository
and three libraries:

```gradle
repositories {
    // ...keep the existing entries...
    maven { url = 'https://maven.brott.dev/' } // FTC Dashboard
}

dependencies {
    // ...keep the existing entries...
    implementation 'com.acmerobotics.dashboard:dashboard:0.4.16'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.13.4.2'
    implementation 'org.ejml:ejml-all:0.38'
}
```

Then run a Gradle sync in Android Studio.

| Library | Used for |
| --- | --- |
| FTC Dashboard | Telemetry and live-editable parameters |
| Jackson | Reading `MANIFEST.JSON` and `MODEL.JSON` from the SD card |
| EJML | Matrix maths in the optimizer and the PID |

## 3. Set up the robot configuration

On the Driver Station, create a configuration that uses these exact names:

| Device | Type | Name |
| --- | --- | --- |
| Odometry computer | goBILDA Pinpoint (I²C port) | `odo` |
| Front-right drive motor | Motor | `FR` |
| Front-left drive motor | Motor | `FL` |
| Back-right drive motor | Motor | `BR` |
| Back-left drive motor | Motor | `BL` |

To use different names, change them in `editables/Hardware.java`. Add any other mechanisms there too.

## 4. Fill in `RobotParams`
{: #robotparams }

`editables/RobotParams.java` holds your robot's constants:

| Field | Meaning |
| --- | --- |
| `chassis` | Drivetrain type. Leave this as `MECANUM`; it is the only one implemented. |
| `distanceUnit` | Unit of the odometry offsets below. |
| `odoXOffset` | How far **sideways** the X (forward) pod is from the robot's tracking centre. Left is positive. |
| `odoYOffset` | How far **forward** the Y (strafe) pod is from the tracking centre. Forward is positive. |
| `podType` | Which goBILDA pods you use. |
| `xEncoderDirection`, `yEncoderDirection` | Flip these so that pushing the robot **forward** increases x, and pushing it **left** increases y. |
| `FRDirection` … `BLDirection` | Flip these so that a **positive** power drives each wheel **forward**. |

{: .tip }
Get the odometry offsets right before you calibrate. A mis-set tracking point leaks into the
drivetrain fit and makes the model worse.

All of these are also editable from FTC Dashboard, but they are only read when an opMode is
initialised.

## 5. Put the SD card in

Insert the microSD card with your tables into the Control Hub. See [The SD card](sd-card.md) for what
goes on it.

{: .note }
By default every Peregrine opMode loads the tables at INIT, so it needs a card with `MANIFEST.JSON`
and `MODEL.JSON` on it. opModes that never drive to targets can
[opt out](opmodes.md#opting-out-of-the-sd-card-tables), and the built-in Calibration teleop already
does.

## 6. Write your first opMode

Continue to [Writing opModes](opmodes.md), or follow the step-by-step
[Learn Peregrine](learn/index.md) tutorial.
