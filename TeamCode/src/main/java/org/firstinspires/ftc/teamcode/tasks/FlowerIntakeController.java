package org.firstinspires.ftc.teamcode.tasks;

import static org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams.positionBlockerClosed;
import static org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams.positionBlockerOpen;
import static org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams.positionFlowerIntakeClosed;
import static org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams.positionFlowerIntakeOpen;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

public class FlowerIntakeController extends Task {

    public FlowerIntakeController (PeregrineOpMode opMode) {
        this.opMode = opMode;
    }

    @Override
    public boolean run() {
        opMode.hardware.flowerIntake.setPosition(positionFlowerIntakeOpen);

        return false;
    }

    @Override
    public void end() {
        opMode.hardware.blocker.setPosition(positionFlowerIntakeClosed);
    }

    @Override
    public Task reset() {

        return new FlowerIntakeController(opMode);
    }

}
