package org.firstinspires.ftc.teamcode.debug;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.opMode;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.PIDHold;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

@Config
@Autonomous
public class PIDHoldDebug extends PeregrineOpMode {

    public static double x_0 = 154.826;
    public static double y_0 = -42.406;
    public static double h_0 = 3.14159;

    public static double x_f = 52.191;
    public static double y_f = -143.518;
    public static double h_f = -1.570;

    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, x_0, y_0, AngleUnit.RADIANS, h_0);
    }

    @Override
    public Task defineTasks() {
        return new PIDHold(this, new Pose2D(DistanceUnit.CM, x_f, y_f, AngleUnit.RADIANS, h_f));
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
        telem.addData("x", localizer.getPose().getX(DistanceUnit.CM));
        telem.addData("y", localizer.getPose().getY(DistanceUnit.CM));
        telem.addData("h", localizer.getPose().getHeading(AngleUnit.RADIANS));
    }

    @Override
    public void end() {

    }
}
