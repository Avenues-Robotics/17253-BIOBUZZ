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
import org.firstinspires.ftc.teamcode.peregrine.editables.GlobalVariables;
import org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams;
import org.firstinspires.ftc.teamcode.tasks.BlockerController;
import org.firstinspires.ftc.teamcode.tasks.SOTMPoseSupplier;
import org.firstinspires.ftc.teamcode.tasks.ShootOffTheMove;
import org.firstinspires.ftc.teamcode.tasks.ShootOnTheMove;
import org.firstinspires.ftc.teamcode.tasks.TransferController;

/**
 * Red alliance teleop. The driver drives with gamepad1, the launcher aims and spins up on its own
 * with shooting on the move, and holding the right trigger fires a volley.
 *
 * <p>The transfer runs constantly in the background, except while a firing sequence is running.</p>
 */
@TeleOp
@Config
public class RedTeleop extends PeregrineOpMode {

    // True while a firing sequence is running, which pauses the background transfer.
    boolean sequenceActive;
    // How long to wait after pausing the background transfer before opening the blocker.
    public static int windDownDelay = 100;//ms
    // How long after the blocker starts opening to start feeding balls.
    public static int blockerDelay = 100; //ms

    // How long the blocker stays open per volley, in ms.
    public static int launchDuration = 500;

    /** @return the field origin; teleop doesn't assume a known start */
    @Override
    public Pose2D startingPose() {

        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    /**
     * Builds the teleop tasks.
     *
     * @return a ParallelTask of driving, shooting on the move, and the transfer controls
     */
    @Override
    public Task defineTasks() {
        ShootOnTheMove shootOnTheMove = new ShootOnTheMove(this);
        SOTMPoseSupplier sotmPoseSupplier = new SOTMPoseSupplier(this, shootOnTheMove);

        // Runs the transfer constantly, except while a firing sequence is running.
        TeleopTask transferController = new TeleopTask(
                this, new TransferController(this, 1),
                () -> !sequenceActive,false);

        Task teleopMovement = new TeleopMovement(this);

        // Fires a volley when the right trigger is pressed. Same steps as FiringSequence, but uses this
        // opmode's own sequenceActive flag and Dashboard-tunable delays.
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
                sotmPoseSupplier,
                shootOnTheMove,
                transferSequence,
                transferController,
                teleopMovement
        );
    }

    /** Closes the blocker and sets the alliance to red so the right hive is targeted. */
    @Override
    public void initStart() {
        hardware.blocker.setPosition(RobotParams.positionBlockerClosed);
        globalVariables.alliance = GlobalVariables.Alliance.RED;
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
