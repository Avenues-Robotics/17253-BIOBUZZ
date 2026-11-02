package org.firstinspires.ftc.teamcode.tasks;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.peregrine.editables.GlobalVariables;

public class SOTMPoseSupplier extends Task {

    ShootOnTheMove shootOnTheMove;

    public SOTMPoseSupplier(PeregrineOpMode opMode, ShootOnTheMove shootOnTheMove) {
        this.opMode = opMode;
        this.shootOnTheMove = shootOnTheMove;
        shootOnTheMove.setHivePose(new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0));
    }

    @Override
    public boolean run() {
        shootOnTheMove.setHivePose(new Pose2D(DistanceUnit.CM,
                opMode.globalVariables.alliance == GlobalVariables.Alliance.RED ? -31.29 : 31.29,
                opMode.localizer.getPose().getX(DistanceUnit.CM) > 0 ? 49.64 : -49.64,
                AngleUnit.RADIANS, 0));
        return false;
    }

    @Override
    public void end() {

    }

    @Override
    public Task reset() {
        return new SOTMPoseSupplier(opMode, shootOnTheMove);
    }
}
