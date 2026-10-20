package org.firstinspires.ftc.teamcode.tasks;

import static org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams.positionBlockerClosed;
import static org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams.positionBlockerOpen;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

public class BlockerController extends Task{

    public BlockerController (PeregrineOpMode opMode) {
        this.opMode = opMode;
    }

    @Override
    public boolean run() {
        opMode.hardware.blocker.setPosition(positionBlockerOpen);

        return false;
    }

    @Override
    public void end() {
        opMode.hardware.blocker.setPosition(positionBlockerClosed);
    }

    @Override
    public Task reset() {

        return new BlockerController(opMode);
    }
}
