package org.firstinspires.ftc.teamcode.tasks;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
public class TransferController extends Task {
    double powerTransfer;   //power to be set to transfer

    public TransferController(PeregrineOpMode opMode, double powerTransfer) {
        this.opMode = opMode;
        this.powerTransfer = powerTransfer;
    }

    @Override
    public boolean run() {
        opMode.hardware.transferR.setPower(powerTransfer);
        opMode.hardware.transferL.setPower(powerTransfer);
        return false;
    }

    @Override
    public void end() {
        opMode.hardware.transferR.setPower(0);
        opMode.hardware.transferL.setPower(0);
    }

    @Override
    public Task reset() {

       return new TransferController(opMode, powerTransfer);
    }

}
