package org.firstinspires.ftc.teamcode.tasks;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

/**
 * Runs both transfer motors at a fixed power for as long as this task runs, and stops them when it
 * ends.
 *
 * <p>The transfer moves balls from the intake toward the flywheel. This task never finishes on its
 * own, so it is usually wrapped in a TeleopTask or a ParallelRaceTask to decide when it stops.</p>
 */
public class TransferController extends Task {
    double powerTransfer;   //power to be set to transfer

    /**
     * Creates a TransferController.
     *
     * @param opMode the opMode whose transfer motors this task drives
     * @param powerTransfer the power, from -1 to 1, to run both transfer motors at
     */
    public TransferController(PeregrineOpMode opMode, double powerTransfer) {
        this.opMode = opMode;
        this.powerTransfer = powerTransfer;
    }

    /**
     * Sets both transfer motors to {@code powerTransfer}.
     *
     * @return false, since this task never finishes on its own
     */
    @Override
    public boolean run() {
        opMode.hardware.transferR.setPower(powerTransfer);
        opMode.hardware.transferL.setPower(powerTransfer);
        return false;
    }

    /** Stops both transfer motors. */
    @Override
    public void end() {
        opMode.hardware.transferR.setPower(0);
        opMode.hardware.transferL.setPower(0);
    }

    /** @return a fresh TransferController with the same opMode and power */
    @Override
    public Task reset() {

       return new TransferController(opMode, powerTransfer);
    }

}
