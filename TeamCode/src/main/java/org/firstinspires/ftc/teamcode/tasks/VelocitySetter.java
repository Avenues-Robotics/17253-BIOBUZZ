package org.firstinspires.ftc.teamcode.tasks;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

public class VelocitySetter extends Task {

    double Vcurr;   //current velocity
    double Vtarg;   //target velocity
    double Vtargoff;    //offset target velocity
    double fullSpeed;   //speed of the motor at full power
    double Tprev;   //previous cycle time
    double Dprev;   //previous cycle distance of motor
    double Dcurr;   //current cycle distance of motor
    static final double ticksPerRev= 537.7; //find according to specific model ADD IN
    static final double wheelDiameter = 0.1;  //meters ADD IN
    static final double wheelCircumference = Math.PI * wheelDiameter;   //meters
    ElapsedTime runtime = new ElapsedTime();
    DcMotor motor = hardwareMap.get(DcMotor.class, "gobilda_motor");
    DcMotor leftMotor;

    //make configurable
    public double targetOffset;
    public double topMargin;


    int prevTicks = 0;

    @Override
    public boolean run() {

        //run during initialization FIX
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");

        int currTicks = motor.getCurrentPosition();

        //difference in d and t
        Dcurr = (((currTicks-prevTicks) / ticksPerRev) * wheelCircumference);

        Vcurr = (Dprev-Dcurr)/(runtime.seconds()-Tprev);   //current velocity
        Vtargoff = 1/fullSpeed * Vtarg;     //offset calculation

        //setting power to motor
        if (Vcurr < Vtarg){
            leftMotor.setPower(1);
        }
        else if (Vcurr > Vtarg+topMargin){
            leftMotor.setPower(-1);
        }
        else{
            leftMotor.setPower(Vtargoff-targetOffset);
        }

        telemetry.addData("Current Velocity", Vcurr);
        telemetry.update();

        //saving current cycle values for next cycle
        Tprev = runtime.seconds();
        Dprev = Dcurr;
        prevTicks = currTicks;

        return false;
    }

    @Override
    public void end() {

    }

    @Override
    public Task reset() {
        return null;
    }
}
