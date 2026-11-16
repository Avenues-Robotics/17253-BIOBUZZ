package org.firstinspires.ftc.teamcode.tasks;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

/**
 * Aims the launcher while the robot is moving by aiming at a "virtual" hive that accounts for the
 * robot's velocity.
 *
 * <p>A ball leaves with the robot's velocity added to it, so aiming straight at the hive would miss.
 * Instead, this task shifts the hive backwards by (robot velocity * flight time) and hands that
 * shifted pose to a ShootOffTheMove, which does the actual aiming and flywheel speed. Flight time
 * depends on distance, and distance depends on the shifted pose, so the two are solved together by
 * iterating a few times.</p>
 *
 * <p>Call {@link #setHivePose(Pose2D)} before this runs (SOTMPoseSupplier does this every loop).
 * It owns its ShootOffTheMove, so don't also add one to the task tree. It never finishes on its own.</p>
 */
@Config
public class ShootOnTheMove extends Task {

    // Does the actual aiming, pointed at the virtual hive computed in run().
    ShootOffTheMove shootOffTheMove;
    // The real hive position in the field frame.
    Pose2D hivePose;

    // How many times to refine the flight time / virtual hive estimate each loop.
    static final int iterations = 4;

    // Flight time fit: t = a * d^2 + b * d + c, where d is the distance to the (virtual) hive in cm.
    public static double a = 0;
    public static double b = 0.00318182;
    public static double c = 0.190909;

    /**
     * Creates a ShootOnTheMove with no hive set yet.
     *
     * @param opMode the opMode whose localizer and launcher this task uses
     */
    public ShootOnTheMove(PeregrineOpMode opMode) {
        this.opMode = opMode;
        shootOffTheMove = new ShootOffTheMove(opMode);
    }

    /**
     * Sets where the real hive is.
     *
     * @param hivePose the hive's pose in the field frame; only x and y are used for aiming
     */
    public void setHivePose(Pose2D hivePose) {
        this.hivePose = hivePose;
    }

    /**
     * Computes the virtual hive from the robot's current pose and velocity, then runs
     * ShootOffTheMove at it for one loop.
     *
     * @return false, since this task never finishes on its own
     * @throws NullPointerException if no hive pose has been set
     */
    @Override
    public boolean run() {
        if (hivePose == null) throw new NullPointerException("Please specify a hive position before running this task");
        Pose2D pose = opMode.localizer.getPose();
        // Robot velocity in the field frame, cm/s.
        double xVel = opMode.localizer.getVelX(DistanceUnit.CM);
        double yVel = opMode.localizer.getVelY(DistanceUnit.CM);

        // Virtual hive position. Starts at the real hive and is refined in the loop below.
        double hiveXV = hivePose.getX(DistanceUnit.CM);
        double hiveYV = hivePose.getY(DistanceUnit.CM);

        opMode.telem.addData("HiveX", hiveXV);
        opMode.telem.addData("HiveY", hiveYV);

        // Distance to the real hive, used as the first guess.
        double d_r = Math.sqrt(Math.pow(pose.getX(DistanceUnit.CM) - hivePose.getX(DistanceUnit.CM), 2) +
                Math.pow(pose.getY(DistanceUnit.CM) - hivePose.getY(DistanceUnit.CM), 2));
        double t = 0; // estimated flight time
        double d_v = d_r; // distance to the virtual hive
        // Fixed-point iteration: distance -> flight time -> virtual hive -> new distance.
        for (int i = 0; i < iterations; i++) {
            t = a*Math.pow(d_v, 2) + b * d_v + c;
            // Shift the hive opposite the robot's motion by how far the robot carries the ball.
            hiveXV = hivePose.getX(DistanceUnit.CM) - xVel * t;
            hiveYV = hivePose.getY(DistanceUnit.CM) - yVel * t;
            d_v = Math.sqrt(Math.pow(pose.getX(DistanceUnit.CM) - hiveXV, 2)
                    + Math.pow(pose.getY(DistanceUnit.CM) - hiveYV, 2));
        }
        // Aim at the virtual hive as if the robot were standing still.
        shootOffTheMove.setHivePose(new Pose2D(DistanceUnit.CM, hiveXV, hiveYV, AngleUnit.RADIANS, hivePose.getHeading(AngleUnit.RADIANS)));
        shootOffTheMove.run();
        return false;
    }

    /** Ends the inner ShootOffTheMove, which stops the launcher. */
    @Override
    public void end() {
        shootOffTheMove.end();
    }

    /** @return a fresh ShootOnTheMove aimed at the same hive */
    @Override
    public Task reset() {
        ShootOnTheMove output = new ShootOnTheMove(opMode);
        output.setHivePose(hivePose);
        return output;
    }

}
