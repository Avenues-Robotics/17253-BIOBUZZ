package org.firstinspires.ftc.teamcode.tasks;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

@Config
public class LauncherHeadingPID extends Task {

    // PIDF Coefficients
    public static double kp; //power per tooth
    public static double ki;
    public static double kd;
    public static double kf;
    public static double ticksPerRev; //test ples
    double integral; // Integral term accumulation
    double previousError; // Previous error value

    //angle values
    double thetaTarget;
    double thetaCurrent;

    int launcherTeeth; //number of teeth (220)
    double motorPinionTeeth; //pinion teeth (48)
    double radPerTooth;
    ElapsedTime runtime;
    double deltaTime; //difference between cycles
    double Tprev;
    double errorRad; //in degrees
    double error; //in teeth
    double output;

    public LauncherHeadingPID(PeregrineOpMode opMode) {

        this.opMode = opMode;
        runtime = new ElapsedTime();
        thetaTarget = 0;
        thetaCurrent = 0;
        integral = 0;
        previousError = 0;
        deltaTime = 0;
        Tprev = 0;
        errorRad = 0;
        error = 0;
        output = 0;

        //variable variables depending on application
        launcherTeeth = 220;
        motorPinionTeeth = 48.0;

        radPerTooth = ((360.0 * Math.PI)/180.0)/launcherTeeth;
    }
    public void setTheta(double thetaTarget){
        this.thetaTarget = thetaTarget;

    }

    //Current launcher heading in degrees, computed from the motor encoder
    private double readTheta() {
        int ticks = opMode.hardware.declinationMotor.getCurrentPosition();
        double motorRevs = ticks / ticksPerRev;
        double teethTraveled = motorRevs * motorPinionTeeth;
        return teethTraveled * radPerTooth; // radians
    }

    @Override
    public boolean run() {

        //calculation of current theta:
        thetaCurrent = mod((readTheta()-Math.PI), (2 * Math.PI));
        thetaTarget = mod((thetaTarget-Math.PI), (2 * Math.PI));

        errorRad = thetaTarget - thetaCurrent;
        error = errorRad / radPerTooth;

        // Proportional term
        double proportionalTerm = kp * error;

        //deltaTime calculation
        deltaTime = runtime.seconds()-Tprev;

        // Integral term
        integral += error * deltaTime;
        double integralTerm = ki * integral;

        // Derivative term
        double derivativeTerm = kd * ((error - previousError) / deltaTime);

        // Feedforward: push in the direction of the error to overcome static friction
        double feedforward = kf * Math.signum(error);

        //output
        output = proportionalTerm + integralTerm + derivativeTerm + feedforward;
        opMode.hardware.declinationMotor.setPower(output);

        // Update previous error value
        previousError = error;

        Tprev = runtime.seconds();

        return false;
    }

    @Override
    public void end() {


    }

    @Override
    public Task reset() {


        return null;
    }

    private double mod(double a, double b) {
        return ((a % b) + b) % b;
    }
}
