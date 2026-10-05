package org.firstinspires.ftc.teamcode.peregrine.core.tasks;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Kinematics;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

/**
 * <h3>Basic robot-centric mecanum teleop from gamepad1.</h3>
 *
 * <p>The left stick drives and strafes, and the right stick's x axis turns. Each stick axis is cubed,
 * which keeps its sign but gives finer control near centre, before it is sent to
 * Kinematics.powerMotors. Never finishes on its own.</p>
 */
public class TeleopMovement extends Task {

    public TeleopMovement(PeregrineOpMode opMode) {
        this.opMode = opMode;
    }

    @Override
    public boolean run() {
        // powerMotors(y = forward, x = strafe, h = turn).
        Kinematics.powerMotors(Math.pow(-opMode.gamepad1.left_stick_y, 3), Math.pow(-opMode.gamepad1.left_stick_x, 3), Math.pow(opMode.gamepad1.right_stick_x, 3), opMode);
        return false;
    }

    // Stops the drive motors.
    @Override
    public void end() {
        Kinematics.powerMotors(0, 0, 0, opMode);
    }

    @Override
    public Task reset() {
        return new TeleopMovement(opMode);
    }
}
