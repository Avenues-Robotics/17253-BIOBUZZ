package org.firstinspires.ftc.teamcode.tasks;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

/**
 * <h3>Aims the launcher at the hive and sets the flywheel speed for the distance to it.</h3>
 *
 * <p>Every loop it takes the robot's pose, then:</p>
 * <ul>
 *     <li>sets the flywheel velocity from a quadratic fit of speed (ticks per second) against
 *     distance to the hive (cm), and</li>
 *     <li>points the turret at the hive by giving LauncherHeadingPID the field bearing to the hive
 *     minus the robot's heading.</li>
 * </ul>
 *
 * <p>It owns and runs its own LauncherVelocityController and LauncherHeadingPID, so don't also add
 * those to the task tree. It does not yet account for the robot's velocity. It never finishes on
 * its own.</p>
 */
@Config
public class ShootOffTheMove extends Task {

    // Flywheel speed fit: speed (ticks/s) = speedQuadratic * d^2 + speedLinear * d + speedConstant,
    // where d is the distance to the hive in cm.
    public static double speedQuadratic = 0.0148148;
    public static double speedLinear = -1.9037;
    public static double speedConstant = 1475.94815;

    // Child controllers, run from this task's run() rather than from the task tree.
    LauncherVelocityController launcherVelocityController;
    LauncherHeadingPID launcherHeadingPID;

    Pose2D pose; // robot pose used for aiming, refreshed from the Localizer every loop when realPose is true
    Pose2D hivePose; // field position of the hive; only its x and y are used

    boolean realPose; // true to read the pose from the Localizer, false to use the pose given to setPose()

    public double distance;

    /**
     * Sets where the hive is. Defaults to the field origin.
     *
     * @param hivePose the hive's position in the field frame; its heading is ignored
     */
    public void setHivePose(Pose2D hivePose) {
        this.hivePose = hivePose;
    }

    /**
     * Creates a ShootOffTheMove that reads the robot's pose from the Localizer, aiming at a hive at
     * the field origin until {@link #setHivePose(Pose2D)} is called.
     *
     * @param opMode the opMode whose hardware and localizer this task uses
     */
    public ShootOffTheMove(PeregrineOpMode opMode) {
        this.opMode = opMode;
        launcherVelocityController = new LauncherVelocityController(opMode);
        launcherHeadingPID = new LauncherHeadingPID(opMode);
        hivePose = new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
        realPose = true;
    }

    /**
     * Updates the flywheel speed and turret target from the current pose, then runs both controllers
     * for one loop.
     *
     * @return always false, this task never finishes on its own
     */
    @Override
    public boolean run() {
        if (realPose) pose = opMode.localizer.getPose();
        // Straight-line distance from the robot to the hive, in cm.
        distance = Math.hypot(pose.getX(DistanceUnit.CM) - hivePose.getX(DistanceUnit.CM),
                pose.getY(DistanceUnit.CM) - hivePose.getY(DistanceUnit.CM));
        launcherVelocityController.setVtarg(speedQuadratic * distance * distance +
                speedLinear * distance + speedConstant);
        opMode.telem.addData("Commanded Velocity",
                speedQuadratic * distance * distance + speedLinear * distance + speedConstant);
        // Field bearing from the robot to the hive, minus the robot's heading, gives the turret
        // heading relative to the robot.
        launcherHeadingPID.setTheta(Math.atan2(
                hivePose.getY(DistanceUnit.CM) - pose.getY(DistanceUnit.CM),
                hivePose.getX(DistanceUnit.CM) - pose.getX(DistanceUnit.CM))
                - pose.getHeading(AngleUnit.RADIANS));
        launcherVelocityController.run();
        launcherHeadingPID.run();
        return false;
    }

    /** Ends both child controllers, which stops the flywheel and the turret. */
    @Override
    public void end() {
        launcherVelocityController.end();
        launcherHeadingPID.end();
    }

    /**
     * @return a new ShootOffTheMove with fresh controllers, keeping this one's realPose, hive pose
     * and current pose
     */
    @Override
    public Task reset() {
        ShootOffTheMove output = new ShootOffTheMove(opMode);
        output.hivePose = this.hivePose;
        output.pose = this.pose;
        return output;
    }
}
