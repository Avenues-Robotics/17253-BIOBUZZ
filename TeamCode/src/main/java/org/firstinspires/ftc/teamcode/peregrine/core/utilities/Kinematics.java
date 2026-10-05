package org.firstinspires.ftc.teamcode.peregrine.core.utilities;

import org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams;

/** Converts a robot-centric (body frame) command into individual wheel powers. */
public final class Kinematics {

    /**
     * Sets the drive motor powers for a body-frame command. This is the same {@code [fwd, strafe, turn]}
     * command space that MODEL.JSON's "control" block uses (TABLE_FORMAT.MD §8.1).
     *
     * <p>If any wheel would get more than full power, all four are scaled down together so the robot
     * still moves in the requested direction. Only {@code MECANUM} is implemented; for any other
     * {@link RobotParams#chassis} this does nothing.</p>
     *
     * @param y forward command, -1..1
     * @param x strafe command, -1..1
     * @param h turn command, -1..1
     * @param opMode the running opMode, whose {@code hardware} holds the drive motors
     */
    public static void powerMotors(double y, double x, double h, PeregrineOpMode opMode) {
        switch(RobotParams.chassis) {
            case MECANUM:

                // Combine the forward, strafe and turn commands to determine each wheel's power.
                double fr = y + x - h;
                double fl = y - x + h;
                double br = y - x - h;
                double bl = y + x + h;

                // Normalize the values so no wheel power exceeds 100%
                double max = Math.max(Math.max(Math.max(Math.abs(fr), Math.abs(fl)), Math.abs(br)), Math.abs(bl));

                if (max > 1) {
                    fr /= max;
                    fl /= max;
                    br /= max;
                    bl /= max;
                }

                opMode.hardware.FR.setPower(fr);
                opMode.hardware.FL.setPower(fl);
                opMode.hardware.BR.setPower(br);
                opMode.hardware.BL.setPower(bl);

                break;
            default:
                // Other chassis types are not implemented, so the command is silently ignored.
                //freak the frickity flip out and crash and burn and explode
                break;
        }
    }

}
