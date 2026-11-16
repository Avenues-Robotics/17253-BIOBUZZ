package org.firstinspires.ftc.teamcode.debug;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Debug opmode for finding servo positions. Set {@code name} to the servo's hardware map name and
 * adjust {@code position} live from FTC Dashboard; the servo follows it until the opmode stops.
 * Useful for picking values like RobotParams.positionBlockerOpen/Closed.
 */
@Config
@TeleOp
public class ServoDebug extends LinearOpMode {

    // Position to command, 0 to 1.
    public static double position = 0;
    // Hardware map name of the servo to test. Must be set before INIT.
    public static String name = "";

    /** Gets the named servo, then sets it to {@code position} every loop until stopped. */
    @Override
    public void runOpMode() {
        Servo servo = hardwareMap.get(Servo.class, name);

        waitForStart();

        while(opModeIsActive()) {
            servo.setPosition(position);
        }
    }
}
