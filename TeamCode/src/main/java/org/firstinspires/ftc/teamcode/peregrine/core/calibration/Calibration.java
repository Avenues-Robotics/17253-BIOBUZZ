package org.firstinspires.ftc.teamcode.peregrine.core.calibration;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.Logger;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelRaceTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopMovement;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams;

/**
 * <h3>Drivetrain calibration teleop</h3>
 *
 * <p>The driver drives by hand with TeleopMovement while a Logger writes every loop's motor powers,
 * pose and velocity to a new {@code logs/log_*.csv} file on the SD card. The desktop fitter
 * ({@code calibration/fit_drivetrain.py}) regresses those logs into the drivetrain model stored in
 * MODEL.JSON (TABLE_FORMAT.MD §8.6). Drive on the same surface you will compete on, and make sure some
 * of the run pushes past the traction limit so the knee can be fit.</p>
 *
 * <p>This opMode doesn't build the OptimalityEngine, so it only needs an SD card to write logs to, not
 * the pathing tables.</p>
 */
@TeleOp
public class Calibration extends PeregrineOpMode {

    TeleopMovement movement;
    Logger logger;

    @Override
    public Task defineTasks() {
        movement = new TeleopMovement(this);
        logger = new Logger(this);
        logger.addDrivetrainItems();

        // Neither task ever returns true, so the race runs until the opMode is stopped.
        return new ParallelRaceTask(movement, logger);
    }

    @Override
    public void initStart() {

    }

    // Calibration only needs relative motion, so start at the origin.
    @Override
    public Pose2D startingPose() {
        return new Pose2D(RobotParams.distanceUnit, 0, 0, AngleUnit.RADIANS, 0);
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

    // Calibration never drives to a target, so it doesn't load the tables.
    @Override
    public boolean buildOptimalityEngine() { return false; } // usually protected
}
