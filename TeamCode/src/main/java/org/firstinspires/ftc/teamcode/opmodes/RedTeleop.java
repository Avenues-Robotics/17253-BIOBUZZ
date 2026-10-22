package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopMovement;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopTask;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.tasks.BlockerController;
import org.firstinspires.ftc.teamcode.tasks.ShootOffTheMove;
import org.firstinspires.ftc.teamcode.tasks.TransferController;

@TeleOp
public class RedTeleop extends PeregrineOpMode {

    @Override
    public Pose2D startingPose() {

        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    @Override
    public Task defineTasks() {
        Task shootOffTheMove = new ShootOffTheMove(this);
        TeleopTask blockerController = new TeleopTask(
                this, new BlockerController(this),() -> gamepad1.dpad_left,false);
        TeleopTask transferController = new TeleopTask(
                this, new TransferController(this, 1),
                () -> gamepad1.left_trigger>0.7,false);
        Task teleopMovement = new TeleopMovement(this);

        return new ParallelTask(shootOffTheMove, blockerController, transferController, teleopMovement);
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

    }

    @Override
    public void end() {

    }
}
