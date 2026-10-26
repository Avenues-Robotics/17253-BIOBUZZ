package org.firstinspires.ftc.teamcode.debug;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.IdleTask;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

/**
 * <h3>Shows the Localizer's pose, for checking odometry.</h3>
 *
 * <p>Does nothing but report the robot's x and y (cm) and heading (radians) every loop. Push the
 * robot around and check the numbers against how far it actually moved.</p>
 */
@TeleOp(group = "QA")
public class LocalizerDebug extends PeregrineOpMode {
    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    @Override
    public Task defineTasks() {
        return new IdleTask();
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
