package org.firstinspires.ftc.teamcode.tasks;

import org.firstinspires.ftc.teamcode.peregrine.core.tasks.InstantTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelRaceTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.SeriesTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.WaitTask;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;

public class FiringSequence extends SeriesTask {

    public FiringSequence(PeregrineOpMode opMode) {
        super(
                new InstantTask(() -> opMode.globalVariables.sequenceActive = true),
                new WaitTask(opMode, 100),
                new ParallelRaceTask(
                        new BlockerController(opMode),
                        new SeriesTask(
                                new WaitTask(opMode, 100),
                                new TransferController(opMode, 1)
                        ),
                        new WaitTask(opMode, 500)
                ),
                new InstantTask(() -> opMode.globalVariables.sequenceActive = false)
        );
    }

}
