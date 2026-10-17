package org.firstinspires.ftc.teamcode.tasks;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

@Config
public class LauncherVelocityController extends Task {

    private double[] pastStates;

    double Vcurr;   //current velocity
    double Vtarg;   //target velocity
    double Vtargoff;    //offset target velocity
    double Tprev;   //previous cycle time
    double Dcurr;   //current cycle distance of motor
    public static double ticksPerRev= 537.7; //find according to specific model
    public static double wheelCircumference = 0.5;   //meters
    public static double fullSpeed = 2.8;   //speed of the motor at full power
    public static double topMargin = 0.1;
    public static double lowerSmoothingFactor = 1;
    public static double upperSmoothingFactor = 1;
    ElapsedTime runtime;

    public LauncherVelocityController(PeregrineOpMode opMode) {
        this.opMode = opMode;
        runtime = new ElapsedTime();
        Vcurr = 0;
        Vtarg = 0;
        Vtargoff = 0;
        Tprev = 0;
        Dcurr = 0;
        pastStates = new double[5];
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

        Vcurr = (Dcurr)/(runtime.seconds()-Tprev);   //current velocity
        Vtargoff = 1/fullSpeed * Vtarg;     //offset calculation

        opMode.telem.addData("Power", opMode.hardware.flywheel.getPower());

        //setting power to motor
        if (Vcurr < Vtarg){
            opMode.hardware.flywheel.setPower(Vtargoff + (Vtarg - Vcurr) * lowerSmoothingFactor);
        }
        else if (Vcurr > Vtarg+topMargin){
            opMode.hardware.flywheel.setPower(Vtargoff - (Vtarg - Vcurr) * upperSmoothingFactor);
        }
        else{
            opMode.hardware.flywheel.setPower(Vtargoff);
        }

        opMode.telem.addData("Current Velocity", Vcurr); //peregrine doesnt use telemetry

        //saving current cycle values for next cycle
        Tprev = runtime.seconds();
        prevTicks = currTicks;

        return false;
    }

    @Override
    public void end() {
        opMode.hardware.flywheel.setPower(0);
    }

    @Override
    public Task reset() {
        return new LauncherVelocityController(opMode);
    }

    private double boxcar(double input) { // dishonest
        // Shift the history one slot toward index 0, then put the newest command in the last slot.
        for (int i = 0; i < pastStates.length - 1; i++) {
            pastStates[i] = pastStates[i+1];
        }
        pastStates[pastStates.length - 1] = input;
        // Mean of each of the 3 command axes over the whole window.
        double sum = pastStates[0];
        for (int j = 1; j < pastStates.length; j++) {
            sum += pastStates[j];
        }
        sum /= pastStates.length;
        return sum;
    }
}
