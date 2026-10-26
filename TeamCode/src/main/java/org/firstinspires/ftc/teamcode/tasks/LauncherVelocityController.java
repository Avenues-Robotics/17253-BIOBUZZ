package org.firstinspires.ftc.teamcode.tasks;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

/**
 * <h3>Holds the launcher flywheel at a target velocity.</h3>
 *
 * <p>The velocity loop itself runs on the hub's built-in motor controller, using the PIDF
 * coefficients Hardware sets from RobotParams. This task only passes the target along with
 * {@link #setVtarg(double)}, reports the measured velocity to telemetry, and stops the flywheel
 * when it ends. It never finishes on its own.</p>
 */
public class LauncherVelocityController extends Task {

    /**
     * @param opMode the opMode whose {@code hardware.flywheel} this task drives
     */
    public LauncherVelocityController(PeregrineOpMode opMode) {
        this.opMode = opMode;
    }

    /**
     * Sets the flywheel's target velocity. It takes effect immediately, without waiting for run().
     *
     * @param Vtarg target velocity in encoder ticks per second
     */
    public void setVtarg(double Vtarg){
        opMode.hardware.flywheel.setVelocity(Vtarg);
    }

    /**
     * Reports the flywheel's measured velocity (ticks per second) to telemetry.
     *
     * @return always false, this task never finishes on its own
     */
    @Override
    public boolean run() {
        opMode.telem.addData("Current Velocity", opMode.hardware.flywheel.getVelocity());
        return false;
    }

    /** Stops the flywheel by setting its target velocity to 0. */
    @Override
    public void end() {
        opMode.hardware.flywheel.setVelocity(0);
    }

    /**
     * @return a new LauncherVelocityController for the same opMode. The flywheel's current target
     * velocity is not cleared.
     */
    @Override
    public Task reset() {
        return new LauncherVelocityController(opMode);
    }
}
