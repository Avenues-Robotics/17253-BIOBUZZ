package org.firstinspires.ftc.teamcode.peregrine.editables;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Enums;

/**
 * <h3>Robot-specific constants.</h3>
 *
 * <p>{@code @Config} makes these editable live from FTC Dashboard. Most are only read when Hardware
 * is constructed, though, so changes take effect on the next opMode init.</p>
 */
@Config
public final class RobotParams {

    // Selects the wheel-power mixing in Kinematics.powerMotors.
    public static Enums.Chassis chassis = Enums.Chassis.MECANUM;

    // Unit for odoXOffset/odoYOffset and Calibration's starting pose. The table code always works in CM.
    public static DistanceUnit distanceUnit = DistanceUnit.CM;

    // Pinpoint pod offsets from the tracking centre, in distanceUnit (see the goBILDA Pinpoint docs for the sign convention).
    public static double odoXOffset = -2.24;
    public static double odoYOffset = -3.92;
    public static GoBildaPinpointDriver.GoBildaOdometryPods podType = GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD;
    public static GoBildaPinpointDriver.EncoderDirection xEncoderDirection = GoBildaPinpointDriver.EncoderDirection.FORWARD;
    public static GoBildaPinpointDriver.EncoderDirection yEncoderDirection = GoBildaPinpointDriver.EncoderDirection.REVERSED;


    // Set so that a positive power drives each wheel forward.
    public static DcMotor.Direction FRDirection = DcMotor.Direction.FORWARD;
    public static DcMotor.Direction FLDirection = DcMotor.Direction.REVERSE;
    public static DcMotor.Direction BRDirection = DcMotor.Direction.FORWARD;
    public static DcMotor.Direction BLDirection = DcMotor.Direction.REVERSE;

}
