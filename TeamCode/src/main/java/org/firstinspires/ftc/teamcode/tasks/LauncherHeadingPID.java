package org.firstinspires.ftc.teamcode.tasks;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

/**
 * <h3>Turns the launcher turret to a target heading with a PIDF loop.</h3>
 *
 * <p>The turret is driven by {@code hardware.declinationMotor} through a 48-tooth pinion on a
 * 220-tooth launcher gear. Its heading is read from that motor's encoder, which Hardware zeroes at
 * INIT, so a heading of 0 is wherever the turret was pointing when INIT was pressed. All angles are
 * in radians, measured relative to the robot.</p>
 *
 * <p>The error is computed in launcher-gear teeth rather than radians, so the gains are in units of
 * motor power per tooth. The turret never crosses the angle {@link #dontGoThrough}: both the current
 * and target headings are shifted so that this angle becomes the 0/2&pi; seam, and the error is taken
 * without wrapping, so the turret goes the long way around rather than through that angle.</p>
 *
 * <p>Set the target with {@link #setTheta(double)}. This task never finishes on its own and is
 * meant to run in parallel with whatever is choosing the target, as ShootOffTheMove does.</p>
 */
@Config
public class LauncherHeadingPID extends Task {

    // PIDF gains, editable live from FTC Dashboard. Output is motor power per tooth of error.
    public static double kp = 0.109; // proportional, power per tooth of error
    public static double ki = 0.00109; // integral, power per tooth-second of accumulated error
    public static double kd = 0.00218; // derivative, power per tooth/second of error change
    public static double kf; // static friction feedforward, applied in the direction of the error (0 by default)
    public static double ticksPerRev = 384.7; // encoder ticks per output shaft revolution of declinationMotor
    public static double dontGoThrough = 1.8; // heading (radians) the turret must never pass through, e.g. a hard stop
    double integral; // running sum of error * deltaTime, in tooth-seconds
    double previousError; // last loop's error in teeth, for the derivative term

    // Headings in radians, after being shifted by dontGoThrough and wrapped into [0, 2pi).
    double thetaTarget;
    double thetaCurrent;

    int launcherTeeth; // teeth on the launcher (turret) gear
    double motorPinionTeeth; // teeth on the motor pinion that drives the launcher gear
    double radPerTooth; // turret rotation per launcher gear tooth
    ElapsedTime runtime; // clock used to time each loop
    double deltaTime; // seconds since the previous call to run()
    double Tprev; // runtime.seconds() at the end of the previous call to run()
    double errorRad; // target minus current heading, in radians
    double error; // target minus current heading, in launcher gear teeth
    double output; // motor power sent to declinationMotor on the last loop
    double ThetaTarget;

    /**
     * Creates the controller with a target heading of 0 and its integral and derivative state cleared.
     *
     * @param opMode the opMode whose {@code hardware.declinationMotor} this task drives
     */
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
        ThetaTarget = 0;


        launcherTeeth = 220;
        motorPinionTeeth = 48.0;

        // One full turn (2pi radians) divided across the launcher gear's teeth.
        radPerTooth = ((360.0 * Math.PI)/180.0)/launcherTeeth;
    }
    /**
     * Sets the heading the turret should turn to.
     *
     * <p>run() shifts the stored target by {@link #dontGoThrough} in place each loop, so call this
     * every loop (as ShootOffTheMove and LauncherHeadingPIDDebug do) to keep the target fixed.
     * TODO: shift a local copy in run() instead, so the stored target stays put.</p>
     *
     * @param thetaTarget target heading in radians, relative to the robot
     */
    public void setTheta(double thetaTarget){
        this.thetaTarget = thetaTarget;

    }

    /**
     * Reads the turret's heading from the declinationMotor encoder by converting motor ticks to
     * motor revolutions, then to launcher gear teeth, then to radians.
     *
     * @return the current turret heading in radians, not wrapped
     */
    private double readTheta() {
        int ticks = opMode.hardware.declinationMotor.getCurrentPosition();
        double motorRevs = ticks / ticksPerRev;
        double teethTraveled = motorRevs * motorPinionTeeth;
        return teethTraveled * radPerTooth; // radians
    }

    /**
     * Runs one loop of the PIDF controller: reads the heading, computes the error in teeth, sets
     * declinationMotor's power and reports the current heading to telemetry.
     *
     * @return always false, this task never finishes on its own
     */
    @Override
    public boolean run() {

        // Shift both headings so dontGoThrough sits at the 0/2pi seam, then wrap into [0, 2pi).
        // Since the error below is not wrapped, the turret can never be told to cross that seam.
        thetaCurrent = mod((readTheta()-dontGoThrough), (2 * Math.PI));
        ThetaTarget = mod((thetaTarget-dontGoThrough), (2 * Math.PI));

        // Error in radians, then converted to launcher gear teeth so the gains are per tooth.
        errorRad = ThetaTarget - thetaCurrent;
        error = errorRad / radPerTooth;

        // Proportional term
        double proportionalTerm = kp * error;

        // Time since the last loop, used by the integral and derivative terms.
        deltaTime = runtime.seconds()-Tprev;

        // Integral term
        integral += error * deltaTime;
        double integralTerm = ki * integral;

        // Derivative term
        double derivativeTerm = kd * ((error - previousError) / deltaTime);

        // Feedforward: a constant push toward the target to overcome static friction.
        double feedforward = kf * Math.signum(error);

        // Sum the terms and send them to the motor as power.
        output = proportionalTerm + integralTerm + derivativeTerm + feedforward;
        opMode.hardware.declinationMotor.setPower(output);

        // Save this loop's error and time for the next loop's derivative and deltaTime.
        previousError = error;

        Tprev = runtime.seconds();

        opMode.telem.addData("Current Theta (radians)", thetaCurrent);

        return false;
    }

    /** Stops the turret by setting declinationMotor's power to 0. Its BRAKE mode then holds it in place. */
    @Override
    public void end() {
        opMode.hardware.declinationMotor.setPower(0);
    }

    /**
     * @return a new LauncherHeadingPID for the same opMode, with a target heading of 0 and its
     * integral and derivative state cleared
     */
    @Override
    public Task reset() {
        return new LauncherHeadingPID(opMode);
    }

    /**
     * Floored modulo: unlike Java's {@code %}, the result has the sign of {@code b}, so a negative
     * angle wraps to a positive one.
     *
     * @param a the dividend
     * @param b the divisor
     * @return {@code a} wrapped into [0, b) for positive {@code b}
     */
    private double mod(double a, double b) {
        return ((a % b) + b) % b;
    }
}
