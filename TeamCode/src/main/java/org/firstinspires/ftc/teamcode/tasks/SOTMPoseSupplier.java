package org.firstinspires.ftc.teamcode.tasks;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.peregrine.editables.GlobalVariables;

/**
 * Keeps a ShootOnTheMove aimed at the right hive by updating its hive pose every loop.
 *
 * <p>Which hive to aim at depends on the alliance (picks the y coordinate) and on which half of the
 * field the robot is in (picks the x coordinate). Run this in parallel with the ShootOnTheMove it
 * feeds. It never finishes on its own.</p>
 */
public class SOTMPoseSupplier extends Task {

    // The task whose hive pose this supplies.
    ShootOnTheMove shootOnTheMove;

    /**
     * Creates the supplier and gives the ShootOnTheMove a placeholder hive at the field origin, so it
     * doesn't throw if it happens to run before this does.
     *
     * @param opMode the opMode whose localizer and globalVariables are read
     * @param shootOnTheMove the task to keep updated
     */
    public SOTMPoseSupplier(PeregrineOpMode opMode, ShootOnTheMove shootOnTheMove) {
        this.opMode = opMode;
        this.shootOnTheMove = shootOnTheMove;
        shootOnTheMove.setHivePose(new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0));
    }

    /**
     * Picks the hive for the current alliance and field side and passes it to the ShootOnTheMove.
     *
     * @return false, since this task never finishes on its own
     */
    @Override
    public boolean run() {
        shootOnTheMove.setHivePose(new Pose2D(DistanceUnit.CM,
                // x: the hive on the same side of the field as the robot.
                opMode.localizer.getPose().getX(DistanceUnit.CM) > 0 ? 49.64 : -49.64,
                // y: the hive belonging to our alliance.
                opMode.globalVariables.alliance == GlobalVariables.Alliance.RED ? 31.29 : -31.29,
                AngleUnit.RADIANS, 0));
        return false;
    }

    /** Nothing to clean up. */
    @Override
    public void end() {

    }

    /** @return a fresh SOTMPoseSupplier feeding the same ShootOnTheMove */
    @Override
    public Task reset() {
        return new SOTMPoseSupplier(opMode, shootOnTheMove);
    }
}
