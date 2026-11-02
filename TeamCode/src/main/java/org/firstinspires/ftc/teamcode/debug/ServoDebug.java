package org.firstinspires.ftc.teamcode.debug;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@Config
@TeleOp
public class ServoDebug extends LinearOpMode {

    public static double position = 0;
    public static String name = "";

    @Override
    public void runOpMode() {
        Servo servo = hardwareMap.get(Servo.class, name);

        waitForStart();

        while(opModeIsActive()) {
            servo.setPosition(position);
        }
    }
}
