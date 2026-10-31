package org.firstinspires.ftc.teamcode.opmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.InstantTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelRaceTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.SeriesTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopMovement;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.WaitTask;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams;
import org.firstinspires.ftc.teamcode.tasks.BlockerController;
import org.firstinspires.ftc.teamcode.tasks.ShootOffTheMove;
import org.firstinspires.ftc.teamcode.tasks.ShootOnTheMove;
import org.firstinspires.ftc.teamcode.tasks.TransferController;

@TeleOp
@Config
public class RedTeleop extends PeregrineOpMode {

    boolean sequenceActive;
    public static int windDownDelay = 100;//ms
    public static int blockerDelay = 100; //ms

    public static int launchDuration = 500;

    @Override
    public Pose2D startingPose() {

        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    @Override
    public Task defineTasks() {
        ShootOnTheMove shootOnTheMove = new ShootOnTheMove(this);
        shootOnTheMove.setHivePose(new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0));
        TeleopTask blockerController = new TeleopTask(this, new BlockerController(this),()->gamepad1.dpad_left, false);

        TeleopTask transferController = new TeleopTask(
                this, new TransferController(this, 1),
                () -> !sequenceActive,false);

        Task teleopMovement = new TeleopMovement(this);

        TeleopTask transferSequence = new TeleopTask (
                this,
                new SeriesTask(
                        new InstantTask(() -> sequenceActive = true),

                        new WaitTask(this, windDownDelay),

                        new ParallelRaceTask(

                                new BlockerController(this),

                                new SeriesTask(

                                    new WaitTask(this, blockerDelay),
                                        new TransferController(this, 1)

                                ),

                                new WaitTask(this, launchDuration)

                                ),

                        new InstantTask(() -> sequenceActive = false)
                ),

                () -> gamepad1.right_trigger>0.7,
                true);

        return new ParallelTask(
                shootOnTheMove,
                transferSequence,
                transferController,
                teleopMovement
        );
    }

    @Override
    public void initStart() {
        hardware.blocker.setPosition(RobotParams.positionBlockerClosed);
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
