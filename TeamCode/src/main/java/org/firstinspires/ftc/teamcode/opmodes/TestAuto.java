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

@Autonomous
public class TestAuto extends PeregrineOpMode {

    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 154.826, -42.406, AngleUnit.RADIANS, Math.PI);
    }

    @Override
    public Task defineTasks() {
        globalVariables.alliance = GlobalVariables.Alliance.BLUE;

        TeleopTask transferController = new TeleopTask(this,
                new TransferController(this, 1), () -> !globalVariables.sequenceActive, false);

        ShootOnTheMove shootOnTheMove = new ShootOnTheMove(this);
        SOTMPoseSupplier sotmPoseSupplier = new SOTMPoseSupplier(this, shootOnTheMove);

        WaitTask wait1 = new WaitTask(this, 5000);
        FiringSequence firingSequence1 = new FiringSequence(this);
        PIDHold flowerIntake = new PIDHold(this, new Pose2D(DistanceUnit.CM, 52.191, -143.518, AngleUnit.RADIANS, -Math.PI/2));
        WaitTask wait2 = new WaitTask(this, 3000);
        PIDHold fire1 = new PIDHold(this, new Pose2D(DistanceUnit.CM, 154.826, -42.406, AngleUnit.RADIANS, Math.PI));
        WaitTask wait3 = new WaitTask(this, 3000);
        FiringSequence firingSequence2 = new FiringSequence(this);
        PIDHold gardenIntakeSetup = new PIDHold(this, new Pose2D(DistanceUnit.CM, 95.229, -130.346, AngleUnit.RADIANS, -0.261));
        WaitTask wait4 = new WaitTask(this, 1000);
        PIDHold gardenIntake = new PIDHold(this, new Pose2D(DistanceUnit.CM, 145.584, -157.174, AngleUnit.RADIANS, 0));
        WaitTask wait5 = new WaitTask(this, 1800);
        PIDHold fire2 = new PIDHold(this, new Pose2D(DistanceUnit.CM, 154.826, -42.406, AngleUnit.RADIANS, Math.PI));
        WaitTask wait6 = new WaitTask(this, 3000);
        FiringSequence firingSequence3 = new FiringSequence(this);
        PIDHold parkSetup = new PIDHold(this, new Pose2D(DistanceUnit.CM, 55.635, -106.986, AngleUnit.RADIANS, -2.799));
        WaitTask wait7 = new WaitTask(this, 1000);
        PIDHold park = new PIDHold(this, new Pose2D(DistanceUnit.CM, -59.960, -142.968, AngleUnit.RADIANS, Math.PI));

        return new ParallelTask(
            new SeriesTask(
                wait1,
                firingSequence1,
                new ParallelRaceTask(flowerIntake, wait2),
                new ParallelRaceTask(fire1, wait3),
                firingSequence2,
                new ParallelRaceTask(gardenIntakeSetup, wait4),
                new ParallelRaceTask(gardenIntake, wait5),
                new ParallelRaceTask(fire2, wait6),
                firingSequence3,
                new ParallelRaceTask(parkSetup, wait7),
                park
            ),
            shootOnTheMove,
            transferController,
            sotmPoseSupplier
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
