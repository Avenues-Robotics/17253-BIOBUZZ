
package org.firstinspires.ftc.teamcode.qa;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopMovement;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams;

@TeleOp(name = "QA Drive (no SD card)", group = "QA")
public class TestTeleop extends PeregrineOpMode {

    // The important part: don't build the OptimalityEngine, so the SD card is never read.
    @Override
    public boolean buildOptimalityEngine() { return false; }

    @Override
    public Pose2D startingPose() {
        return new Pose2D(RobotParams.distanceUnit, 0, 0, AngleUnit.RADIANS, 0);
    }

    @Override
    public Task defineTasks() {
        // TeleopMovement only needs the hardware and gamepads, not the engine.
        return new TeleopMovement(this);
    }

    @Override public void initStart() {}
    @Override public void initLoop() {}
    @Override public void mainStart() {}

    @Override
    public void mainLoop() {
        telem.addData("x (cm)", localizer.getPose().getX(DistanceUnit.CM));
        telem.addData("y (cm)", localizer.getPose().getY(DistanceUnit.CM));
        telem.addData("heading (rad)", localizer.getPose().getHeading(AngleUnit.RADIANS));
    }

    @Override public void end() {}
}