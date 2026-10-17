package org.firstinspires.ftc.teamcode.tasks;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

public class PIDLauncherVelocityController extends Task {

    public PIDLauncherVelocityController (PeregrineOpMode opMode) {
        this.opMode = opMode;
    }

    public void setVtarg(double Vtarg){
        opMode.hardware.flywheel.setVelocity(Vtarg);
    }

    @Override
    public boolean run() {
        opMode.telem.addData("Current Velocity", opMode.hardware.flywheel.getVelocity());
        return false;
    }

    @Override
    public void end() {
        opMode.hardware.flywheel.setVelocity(0);
    }

    @Override
    public Task reset() {
        return new PIDLauncherVelocityController(opMode);
    }
}
