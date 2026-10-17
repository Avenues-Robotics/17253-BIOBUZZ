package org.firstinspires.ftc.teamcode.tasks;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

@Config
public class ShootOffTheMove extends Task {

    public static double speedQuadratic = 1;
    public static double speedLinear = 1;
    public static double speedConstant = 0;

    LauncherVelocityController launcherVelocityController;
    LauncherHeadingPID launcherHeadingPID;

    Pose2D pose;
    Pose2D hivePose;

    boolean realPose;

    public void setHivePose(Pose2D hivePose) {
        this.hivePose = hivePose;
    }

    public void setPose(Pose2D pose) {
        if (realPose) throw new IllegalStateException("May only set pose for a ShootOffTheMove instance with realPose set to false");
        this.pose = pose;
    }

    public ShootOffTheMove(PeregrineOpMode opMode) {
        this.opMode = opMode;
        launcherVelocityController = new LauncherVelocityController(opMode);
        launcherHeadingPID = new LauncherHeadingPID(opMode);
        hivePose = new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
        realPose = true;
    }

    public ShootOffTheMove(PeregrineOpMode opMode, boolean realPose) {
        this(opMode);
        this.realPose = realPose;
        pose = new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    @Override
    public boolean run() {
        if (realPose) pose = opMode.localizer.getPose();
        double distance = Math.hypot(pose.getX(DistanceUnit.CM) - hivePose.getX(DistanceUnit.CM),
                pose.getY(DistanceUnit.CM) - hivePose.getY(DistanceUnit.CM));
        launcherVelocityController.setVtarg(speedQuadratic * distance * distance +
                speedLinear * distance + speedConstant);
        launcherHeadingPID.setTheta(Math.atan2(
                hivePose.getY(DistanceUnit.CM) - pose.getY(DistanceUnit.CM),
                hivePose.getX(DistanceUnit.CM) - pose.getX(DistanceUnit.CM))
                - pose.getHeading(AngleUnit.RADIANS));
        launcherVelocityController.run();
        launcherHeadingPID.run();
        return false;
    }

    @Override
    public void end() {
        launcherVelocityController.end();
        launcherHeadingPID.end();
    }

    @Override
    public Task reset() {
        ShootOffTheMove output = new ShootOffTheMove(opMode, realPose);
        output.hivePose = this.hivePose;
        output.pose = this.pose;
        return output;
    }
}
