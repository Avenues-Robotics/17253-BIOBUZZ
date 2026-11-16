package org.firstinspires.ftc.teamcode.tasks;

import static org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams.positionBlockerClosed;
import static org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams.positionBlockerOpen;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

/**
 * Holds the blocker servo open for as long as this task runs, and closes it again when the task ends.
 *
 * <p>The blocker sits between the transfer and the flywheel, so opening it lets balls through to be
 * launched. This task never finishes on its own; put it in a ParallelRaceTask (as FiringSequence
 * does) to control how long the blocker stays open.</p>
 */
public class BlockerController extends Task{

    /**
     * Creates a BlockerController.
     *
     * @param opMode the opMode whose blocker servo this task controls
     */
    public BlockerController (PeregrineOpMode opMode) {
        this.opMode = opMode;
    }

    /**
     * Sets the blocker to its open position.
     *
     * @return false, since this task never finishes on its own
     */
    @Override
    public boolean run() {
        opMode.hardware.blocker.setPosition(positionBlockerOpen);

        return false;
    }

    /** Returns the blocker to its closed position so no more balls reach the flywheel. */
    @Override
    public void end() {
        opMode.hardware.blocker.setPosition(positionBlockerClosed);
    }

    /** @return a fresh BlockerController on the same opMode */
    @Override
    public Task reset() {

        return new BlockerController(opMode);
    }
}
