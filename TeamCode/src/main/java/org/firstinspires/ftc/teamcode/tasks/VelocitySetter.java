package org.firstinspires.ftc.teamcode.tasks;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

@Config
public class VelocitySetter extends Task {

    double Vcurr;   //current velocity
    double Vtarg;   //target velocity
    double Vtargoff;    //offset target velocity
    double Tprev;   //previous cycle time
    double Dprev;   //previous cycle distance of motor
    double Dcurr;   //current cycle distance of motor
    public static double ticksPerRev= 537.7; //find according to specific model
    public static double wheelDiameter = 0.1;  //meters
    public static double wheelCircumference = Math.PI * wheelDiameter;   //meters
    public static double fullSpeed;   //speed of the motor at full power
    public static double topMargin = 0.1;
    ElapsedTime runtime;

    public VelocitySetter (PeregrineOpMode opMode) {
        this.opMode = opMode;
        runtime = new ElapsedTime();
        Vcurr = 0;
        Vtarg = 0;
        Vtargoff = 0;
        Tprev = 0;
        Dprev = 0;
        Dcurr = 0;

    }

    int prevTicks;

    //function to set Vtarg
    public void setVtarg(double Vtarg){
        this.Vtarg = Vtarg;

    }

    @Override
    public boolean run() {

        int currTicks = opMode.hardware.flywheel.getCurrentPosition();

        Dcurr = (((currTicks-prevTicks) / ticksPerRev) * wheelCircumference);

        Vcurr = (Dcurr-Dprev)/(runtime.seconds()-Tprev);   //current velocity
        Vtargoff = 1/fullSpeed * Vtarg;     //offset calculation

        //setting power to motor
        if (Vcurr < Vtarg){
            opMode.hardware.flywheel.setPower(1);
        }
        else if (Vcurr > Vtarg+topMargin){
            opMode.hardware.flywheel.setPower(-1);
        }
        else{
            opMode.hardware.flywheel.setPower(Vtargoff);
        }

        telemetry.addData("Current Velocity", Vcurr);

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
