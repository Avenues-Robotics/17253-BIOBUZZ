---
title: Your first opMode
parent: Learn Peregrine
nav_order: 3
---

# Your first opMode
{: .no_toc }

You'll write a teleop that lets you drive the robot with a gamepad and shows its position on the
screen.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

## 1. Create the class

In Android Studio's project panel, open `TeamCode/src/main/java/org/firstinspires/ftc/teamcode`.
Right-click the `teamcode` package and choose **New → Java Class**. Name it `MyFirstTeleop`.

You get an empty class. Change the first line so it **extends** `PeregrineOpMode`:

```java
public class MyFirstTeleop extends PeregrineOpMode {
}
```

`PeregrineOpMode` will turn red. Click on it, press **Alt+Enter** (**Option+Enter** on a Mac) and
choose **Import class**. Then the class name `MyFirstTeleop` turns red, because `PeregrineOpMode` is
abstract and you haven't filled in its blanks yet. Click on it, press Alt+Enter again, choose
**Implement methods**, select all seven, and press OK.

## 2. Fill in the blanks

Replace the class with this. The comments explain each part:

```java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopMovement;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

// Shows this opMode in the Driver Station's teleop list.
@TeleOp(name = "My First Teleop")
public class MyFirstTeleop extends PeregrineOpMode {

    // Where the robot is when INIT is pressed. For a simple teleop, (0, 0) facing 0 is fine.
    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    // The task tree. For now it's a single task: drive with gamepad 1.
    @Override
    public Task defineTasks() {
        return new TeleopMovement(this);
    }

    // This opMode never drives to a target, so it doesn't need the SD card tables.
    @Override
    protected boolean buildOptimalityEngine() {
        return false;
    }

    @Override
    public void initStart() {
    }

    @Override
    public void initLoop() {
    }

    @Override
    public void mainStart() {
    }

    // Runs every loop after START. Show where the robot thinks it is.
    @Override
    public void mainLoop() {
        telem.addData("x (cm)", localizer.getPose().getX(DistanceUnit.CM));
        telem.addData("y (cm)", localizer.getPose().getY(DistanceUnit.CM));
    }

    @Override
    public void end() {
    }
}
```

### What's going on

- **`extends PeregrineOpMode`**: your class *is* a Peregrine opMode, so it inherits all the setup and
  the main loop. It also inherits the fields `hardware`, `localizer`, `telem` and `gamepad1`, which is
  why `mainLoop()` can use `telem` and `localizer` without declaring them.
- **`new TeleopMovement(this)`** builds a task object. `this` is your opMode; the task keeps it so it
  can reach the gamepad and the motors. `TeleopMovement` never finishes, so the opMode keeps running
  until you press STOP.
- **`buildOptimalityEngine()`** is a method `PeregrineOpMode` already has (it returns `true`). By
  overriding it to return `false`, you switch off loading the pathing tables. Without this, the
  opMode would stop at INIT with `No SD Card found.` if there's no card.
- The empty methods have to exist because they're abstract in `PeregrineOpMode`, even though this
  opMode doesn't need them yet.

## 3. Run it

1. Press the green **Run** arrow in Android Studio to build and install the app on the robot.
2. On the Driver Station, open the teleop list and choose **My First Teleop**.
3. Press **INIT**. Peregrine waits for the Pinpoint to be ready; if it takes a while you'll see
   `Odo Status` on the screen.
4. Press **START**, and drive: the left stick moves the robot, and the right stick turns it.
5. Watch `x (cm)` and `y (cm)` change on the Driver Station as you drive.

You can also see the telemetry in a browser: connect to the robot's Wi-Fi and open
**http://192.168.43.1:8080/dash**.

## Try it

1. Also show the robot's **heading in degrees**. (Hint: `localizer.getPose().getHeading(...)` and
   `AngleUnit`.)
2. Show the message `"Ready!"` while the opMode is waiting for START.
3. Change `defineTasks()` to `return new EmptyTask();`. Predict what will happen when you press
   START, then try it. Change it back afterwards.

<details markdown="block">
<summary>Answers</summary>

1. In `mainLoop()`:

   ```java
   telem.addData("heading (deg)", localizer.getPose().getHeading(AngleUnit.DEGREES));
   ```

2. In `initLoop()`, which runs over and over until START:

   ```java
   telem.addLine("Ready!");
   ```

3. The opMode ends immediately after START. `EmptyTask` finishes on its first `run()`, so the whole
   tree has finished, and a Peregrine opMode ends when its tree does.

</details>

Next: [Your first task](first-task.md).
