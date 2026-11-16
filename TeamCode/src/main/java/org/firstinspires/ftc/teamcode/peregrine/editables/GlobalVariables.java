package org.firstinspires.ftc.teamcode.peregrine.editables;

/**
 * Put any state you want to share between tasks here, for example a game-element count or a
 * detected randomisation. One instance is created per opMode and is reachable from any task as
 * {@code opMode.globalVariables}.
 */
public class GlobalVariables {

    /** Which alliance this robot is on. Decides which hive SOTMPoseSupplier aims at. */
    public enum Alliance {
        RED,
        BLUE
    }

    // True while a FiringSequence is running. The background transfer pauses while this is true.
    public boolean sequenceActive;
    // This match's alliance. Autos set it in defineTasks(); defaults to RED.
    public Alliance alliance;

    /** Starts with no firing sequence active and the alliance set to RED. */
    public GlobalVariables() {
        sequenceActive = false;
        alliance = Alliance.RED;
    }
}
