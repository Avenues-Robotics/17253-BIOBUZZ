package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.PIDHold;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelRaceTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.SeriesTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.WaitTask;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.peregrine.editables.GlobalVariables;
import org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams;
import org.firstinspires.ftc.teamcode.tasks.FiringSequence;
import org.firstinspires.ftc.teamcode.tasks.SOTMPoseSupplier;
import org.firstinspires.ftc.teamcode.tasks.ShootOnTheMove;
import org.firstinspires.ftc.teamcode.tasks.TransferController;

/**
 * Blue alliance autonomous for when our alliance partner is disabled, so this robot runs the whole
 * route by itself: four volleys, picking up from the garden and both flowers in between, then park.
 *
 * <p>Each move is a PIDHold raced against a WaitTask, so the robot moves on after a fixed time
 * whether or not it has fully settled. Shooting on the move, hive selection, and the background
 * transfer run in parallel for the whole match.</p>
 */
@Autonomous
public class BlueAutoPartnerDisabled extends PeregrineOpMode {
    /** @return the blue right-side starting position, which is also the first shooting spot */
    @Override
    public Pose2D startingPose() {
        return blue_right();
    }

    /**
     * Builds the auto route.
     *
     * @return a ParallelTask running the route alongside the always-on shooting and transfer tasks
     */
    @Override
    public Task defineTasks() {
        globalVariables.alliance = GlobalVariables.Alliance.BLUE;

        // Runs the transfer constantly, except while a FiringSequence has control of it.
        TransferController transferController = new TransferController(this, 1);

        // Keeps the launcher aimed and spun up at the correct hive the whole match.
        ShootOnTheMove shootOnTheMove = new ShootOnTheMove(this);
        SOTMPoseSupplier sotmPoseSupplier = new SOTMPoseSupplier(this, shootOnTheMove);

        // Route steps, in order. Each PIDHold is paired with the WaitTask after it as a timeout.
        // Wait for the flywheel to spin up, then fire the preloads.
        WaitTask wait1 = new WaitTask(this, 2500);
        FiringSequence firingSequence1 = new FiringSequence(this);
        // Line up with the garden, then drive through it to intake.
        PIDHold gardenIntakeSetup = new PIDHold(this, blue_garden_setup());
        WaitTask wait2 = new WaitTask(this, 1000);
        PIDHold gardenIntake = new PIDHold(this, blue_garden());
        WaitTask wait3 = new WaitTask(this, 1800);
        // Come back past the start, then over to the left shooting spot and fire.
        PIDHold leftInjection1 = new PIDHold(this, blue_right());
        WaitTask wait4 = new WaitTask(this, 1500);
        PIDHold shoot2 = new PIDHold(this, blue_left());
        WaitTask wait5 = new WaitTask(this, 3000);
        FiringSequence firingSequence2 = new FiringSequence(this);
        // Go via the park spot to the blue flower to intake.
        PIDHold rightInjection1 = new PIDHold(this, blue_park());
        WaitTask wait6 = new WaitTask(this, 700);
        PIDHold blueFlower = new PIDHold(this, blue_flower());
        WaitTask wait7 = new WaitTask(this, 3000);
        // Back via the park spot to the left shooting spot and fire.
        PIDHold leftInjection2 = new PIDHold(this, blue_park());
        WaitTask wait8 = new WaitTask(this, 1000);
        PIDHold shoot3 = new PIDHold(this, blue_left());
        WaitTask wait9 = new WaitTask(this, 1500);
        FiringSequence firingSequence3 = new FiringSequence(this);
        // Via the park spot to the left flower, intake, and fire from there.
        PIDHold setup = new PIDHold(this, blue_park());
        WaitTask wait10 = new WaitTask(this, 1000);
        PIDHold leftFlower = new PIDHold(this, blue_left_flower());
        WaitTask wait11 = new WaitTask( this, 1500);
        FiringSequence firingSequence4 = new FiringSequence(this);
        // Finish in the park spot.
        PIDHold park = new PIDHold(this, blue_park());


        // The route runs in series; everything else runs alongside it for the whole match.
        return new ParallelTask(
                new SeriesTask(
                        wait1,
                        firingSequence1,
                        new ParallelRaceTask(
                                gardenIntakeSetup,
                                wait2
                        ),
                        new ParallelRaceTask(
                                gardenIntake,
                                wait3
                        ),
                        new ParallelRaceTask(
                                leftInjection1,
                                wait4
                        ),
                        new ParallelRaceTask(
                                shoot2,
                                wait5
                        ),
                        firingSequence2,
                        new ParallelRaceTask(
                                rightInjection1,
                                wait6
                        ),
                        new ParallelRaceTask(
                                blueFlower,
                                wait7
                        ),
                        new ParallelRaceTask(
                                leftInjection2,
                                wait8
                        ),
                        new ParallelRaceTask(
                                shoot3,
                                wait9
                        ),
                        firingSequence3,
                        new ParallelRaceTask(
                                setup,
                                wait10
                        ),
                        new ParallelRaceTask(
                                leftFlower,
                                wait11
                        ),
                        firingSequence4,
                        park
                ),
                transferController,
                shootOnTheMove,
                sotmPoseSupplier
        );
    }

    /** Closes the blocker during INIT so no balls reach the flywheel before a FiringSequence. */
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

    // Named field positions used by the route, in cm and radians in the field frame.

    /** @return the right-side start and shooting position */
    private Pose2D blue_right() {
        return new Pose2D(DistanceUnit.CM, 154.826, -42.406, AngleUnit.RADIANS, Math.PI);
    }

    /** @return the blue flower intake position */
    private Pose2D blue_flower() {
        return new Pose2D(DistanceUnit.CM, 52.191, -143.518, AngleUnit.RADIANS, -Math.PI/2);
    }

    /** @return the end of the garden intake run */
    private Pose2D blue_garden() {
        return new Pose2D(DistanceUnit.CM, 145.584, -157.174, AngleUnit.RADIANS, 0);
    }

    /** @return the park position, also used as a waypoint between other spots */
    private Pose2D blue_park() {
        return new Pose2D(DistanceUnit.CM, -59.960, -142.968, AngleUnit.RADIANS, Math.PI);
    }

    /** @return the left flower intake position */
    private Pose2D blue_left_flower() {
        return new Pose2D(DistanceUnit.CM, -149.156, -58.051, AngleUnit.RADIANS, Math.PI);
    }

    /** @return the left-side shooting position */
    private Pose2D blue_left() {
        return new Pose2D(DistanceUnit.CM, -168.720, -18.409, AngleUnit.RADIANS, 0);
    }

    /** @return the spot that lines the robot up before driving through the garden */
    private Pose2D blue_garden_setup() {
        return new Pose2D(DistanceUnit.CM, 95.229, -130.346, AngleUnit.RADIANS, -0.261);
    }
}
