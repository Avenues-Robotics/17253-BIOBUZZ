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
import org.firstinspires.ftc.teamcode.tasks.LauncherVelocityController;

/**
 * <h3>Tuning opMode for the launcher flywheel.</h3>
 *
 * <p>Runs LauncherVelocityController on its own and sends it {@link #targetSpeed} every loop, while
 * running the right transfer motor at full power to feed the flywheel. Change targetSpeed from FTC
 * Dashboard while it runs, and watch "Current Velocity" in telemetry.</p>
 */
@Config
@Autonomous
public class LauncherVelocityControllerDebug extends PeregrineOpMode {

    // Flywheel target velocity in encoder ticks per second. Editable from FTC Dashboard.
    public static double targetSpeed = 1000;

    /** @return the field origin, since this opMode doesn't use the robot's position */
    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    /**
     * Runs the controller alongside a WaitUntilTask whose condition passes targetSpeed to it every
     * loop and always returns false, so the opMode runs until it is stopped.
     *
     * @return the controller and target updater in parallel
     */
    @Override
    public Task defineTasks() {
        LauncherVelocityController controller = new LauncherVelocityController(this);
        return new ParallelTask(controller, new WaitUntilTask(() -> {controller.setVtarg(targetSpeed); return false;}));
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

    /** Keeps the right transfer motor at full power to feed the flywheel. */
    @Override
    public void mainLoop() {
        hardware.transferR.setPower(1);
    }

    @Override
    public void end() {

    }
}
