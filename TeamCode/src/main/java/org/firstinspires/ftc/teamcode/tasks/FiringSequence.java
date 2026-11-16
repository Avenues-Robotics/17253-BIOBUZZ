package org.firstinspires.ftc.teamcode.tasks;

import org.firstinspires.ftc.teamcode.peregrine.core.tasks.InstantTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelRaceTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.SeriesTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.WaitTask;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;

/**
 * One volley of shots: opens the blocker, feeds balls into the flywheel with the transfer, then
 * closes the blocker again.
 *
 * <p>While it runs, {@code globalVariables.sequenceActive} is true. Opmodes use that flag to pause
 * their always-on transfer (see the TeleopTask around TransferController in the autos), so the
 * transfer can briefly stop and let the blocker open before balls are pushed through.</p>
 *
 * <p>Timeline: set the flag, wait 100 ms for the background transfer to stop, open the blocker,
 * start the transfer 100 ms later, stop everything after 500 ms total, then clear the flag.</p>
 */
public class FiringSequence extends SeriesTask {

    /**
     * Builds the firing sequence.
     *
     * @param opMode the opMode whose hardware and globalVariables this sequence uses
     */
    public FiringSequence(PeregrineOpMode opMode) {
        super(
                // Tell the rest of the opMode a shot is happening, so the background transfer stops.
                new InstantTask(() -> opMode.globalVariables.sequenceActive = true),
                // Give the background transfer time to wind down.
                new WaitTask(opMode, 100),
                // Ends when the 500 ms wait ends, which also closes the blocker and stops the transfer.
                new ParallelRaceTask(
                        new BlockerController(opMode),
                        // Start feeding a little after the blocker starts opening.
                        new SeriesTask(
                                new WaitTask(opMode, 100),
                                new TransferController(opMode, 1)
                        ),
                        new WaitTask(opMode, 500)
                ),
                // Hand the transfer back to the background controller.
                new InstantTask(() -> opMode.globalVariables.sequenceActive = false)
        );
    }

}
