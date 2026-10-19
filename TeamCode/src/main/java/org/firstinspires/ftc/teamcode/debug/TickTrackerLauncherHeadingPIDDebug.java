package org.firstinspires.ftc.teamcode.debug;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.opMode;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.IdleTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.WaitUntilTask;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.tasks.LauncherHeadingPID;



/**
 * <h3>Reads the turret motor's encoder, for measuring LauncherHeadingPID.ticksPerRev.</h3>
 *
 * <p>Does nothing but show declinationMotor's encoder position as "Current Ticks". Hardware zeroes
 * the encoder at INIT, so turn the motor (or turret) by a known amount and read off how many ticks
 * that took.</p>
 */
@Config
@Autonomous
public class TickTrackerLauncherHeadingPIDDebug extends PeregrineOpMode {


    /** @return the field origin, since this opMode doesn't use the robot's position */
    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    /** @return an IdleTask, so the opMode runs until it is stopped without moving anything */
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

    /** Shows declinationMotor's encoder position, in ticks since INIT. */
    @Override
    public void mainLoop() {
        int ticks = hardware.declinationMotor.getCurrentPosition();
        telem.addData("Current Ticks", ticks);

    }

    @Override
    public void end() {

    }
}
