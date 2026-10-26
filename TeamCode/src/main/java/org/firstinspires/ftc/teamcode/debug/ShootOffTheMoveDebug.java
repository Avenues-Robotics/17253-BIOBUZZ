package org.firstinspires.ftc.teamcode.debug;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.tasks.ShootOffTheMove;

/**
 * <h3>Test opMode for ShootOffTheMove.</h3>
 *
 * <p>Runs ShootOffTheMove using the Localizer's pose and the default hive position (the field
 * origin), while running the right transfer motor at full power to feed the flywheel. Push the
 * robot around and check that the turret tracks the hive and the flywheel speed changes with
 * distance.</p>
 */
@Config
@Autonomous
public class ShootOffTheMoveDebug extends PeregrineOpMode {

    /** @return the field origin, which is also where ShootOffTheMove puts the hive by default */
    @Override
    public Pose2D startingPose() {
        return new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0);
    }

    /** @return a ShootOffTheMove that reads its pose from the Localizer and never finishes */
    @Override
    public Task defineTasks() {
        return new ShootOffTheMove(this);
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

    /** Keeps the right transfer motor at full power to feed the flywheel. */
    @Override
    public void mainLoop() {
        hardware.transferR.setPower(1);
    }

    @Override
    public void end() {

    }
}
