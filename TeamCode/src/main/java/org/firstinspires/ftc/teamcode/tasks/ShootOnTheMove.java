package org.firstinspires.ftc.teamcode.tasks;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

@Config
public class ShootOnTheMove extends Task {

    ShootOffTheMove shootOffTheMove;
    Pose2D hivePose;

    static final int iterations = 4;

    public static double a = 0;
    public static double b = 1;
    public static double c = 1;

    public ShootOnTheMove(PeregrineOpMode opMode) {
        this.opMode = opMode;
        shootOffTheMove = new ShootOffTheMove(opMode);
    }

    public void setHivePose(Pose2D hivePose) {
        this.hivePose = hivePose;
    }

    @Override
    public boolean run() {
        if (hivePose == null) throw new NullPointerException("Please specify a hive position before running this task");
        Pose2D pose = opMode.localizer.getPose();
        double xVel = opMode.localizer.getVelX(DistanceUnit.CM);
        double yVel = opMode.localizer.getVelY(DistanceUnit.CM);

        double hiveXV = hivePose.getX(DistanceUnit.CM);
        double hiveYV = hivePose.getY(DistanceUnit.CM);

        double d_r = Math.sqrt(Math.pow(pose.getX(DistanceUnit.CM) - hivePose.getX(DistanceUnit.CM), 2) +
                Math.pow(pose.getY(DistanceUnit.CM) - hivePose.getY(DistanceUnit.CM), 2));
        double t = 0;
        double d_v = d_r;
        for (int i = 0; i < iterations; i++) {
            t = a*Math.pow(d_v, 2) + b * d_v + c;
            hiveXV = hivePose.getX(DistanceUnit.CM) - xVel * t;
            hiveYV = hivePose.getY(DistanceUnit.CM) - yVel * t;
            d_v = Math.sqrt(Math.pow(pose.getX(DistanceUnit.CM) - hiveXV, 2)
                    + Math.pow(pose.getY(DistanceUnit.CM) - hiveYV, 2));
        }
        shootOffTheMove.setHivePose(new Pose2D(DistanceUnit.CM, hiveXV, hiveYV, AngleUnit.RADIANS, hivePose.getHeading(AngleUnit.RADIANS)));
        shootOffTheMove.run();
        return false;
    }

    @Override
    public void end() {
        shootOffTheMove.end();
    }

    @Override
    public Task reset() {
        ShootOnTheMove output = new ShootOnTheMove(opMode);
        output.setHivePose(hivePose);
        return output;
    }

}
