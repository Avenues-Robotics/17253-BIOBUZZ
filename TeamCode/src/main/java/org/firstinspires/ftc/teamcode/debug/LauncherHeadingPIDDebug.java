package org.firstinspires.ftc.teamcode.debug;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.WaitUntilTask;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.tasks.LauncherHeadingPID;

/**
 * <h3>Tuning opMode for LauncherHeadingPID.</h3>
 *
 * <p>Runs LauncherHeadingPID on its own and sends it {@link #targetTheta} every loop. Change
 * targetTheta and the PID gains from FTC Dashboard while it runs, and watch "Current Theta
 * (radians)" in telemetry.</p>
 */
@Config
@Autonomous
public class LauncherHeadingPIDDebug extends PeregrineOpMode {

    // Turret target heading in radians, relative to the robot. Editable from FTC Dashboard.
    public static double targetTheta = 1000;

    /** @return the field origin, since this opMode doesn't use the robot's position */
    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    /**
     * Runs the controller alongside a WaitUntilTask whose condition passes targetTheta to it every
     * loop and always returns false, so the opMode runs until it is stopped.
     *
     * @return the controller and target updater in parallel
     */
    @Override
    public Task defineTasks() {
        LauncherHeadingPID controller = new LauncherHeadingPID(this);
        return new ParallelTask(controller, new WaitUntilTask(() -> {controller.setTheta(targetTheta); return false;}));
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

    @Override
    public void mainLoop() {

    }

    @Override
    public void end() {

    }
}
