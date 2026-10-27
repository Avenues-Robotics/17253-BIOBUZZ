package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.InstantTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.SeriesTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopMovement;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.WaitTask;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.tasks.BlockerController;
import org.firstinspires.ftc.teamcode.tasks.ShootOffTheMove;
import org.firstinspires.ftc.teamcode.tasks.TransferController;

@TeleOp
public class RedTeleop extends PeregrineOpMode {

    boolean sequenceActive;

    @Override
    public Pose2D startingPose() {

        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    @Override
    public Task defineTasks() {
        Task shootOffTheMove = new ShootOffTheMove(this);
        TeleopTask blockerController = new TeleopTask(this, new BlockerController(this),()->gamepad1.dpad_left, false);

        TeleopTask transferController = new TeleopTask(
                this, new TransferController(this, 1),
                () -> gamepad1.left_trigger>0.7 && !sequenceActive,false);

        Task teleopMovement = new TeleopMovement(this);

        TeleopTask transferSequence = new TeleopTask (
                this,
                new SeriesTask(
                        new InstantTask(() -> sequenceActive = true),

                        new InstantTask(() -> {
                            hardware.transferR.setPower(0);
                            hardware.transferL.setPower(0);
                        }),

                        new BlockerController(this),

                        new TransferController(this, 1),

                        new WaitTask(this, 10000),

                        new InstantTask(() -> {
                            hardware.transferR.setPower(1);
                            hardware.transferL.setPower(1);
                            sequenceActive = false;
                        })
                ),
                () -> gamepad1.right_trigger>0.7,
                true);

        return new ParallelTask(
                shootOffTheMove,
                transferSequence,
                transferController,
                teleopMovement
        );
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
