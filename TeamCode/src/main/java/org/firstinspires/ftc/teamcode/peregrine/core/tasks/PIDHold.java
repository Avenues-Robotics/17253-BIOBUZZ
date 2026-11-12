package org.firstinspires.ftc.teamcode.peregrine.core.tasks;

import static androidx.core.math.MathUtils.clamp;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.ejml.simple.SimpleMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Kinematics;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

/**
 * <h3>The honing PID for the final approach to a target (TABLE_FORMAT.MD §8.8).</h3>
 *
 * <p>Implements the §8.8.2 control law {@code u = Kp*e + Ki*i + Kd*de/dt} with the gains shipped
 * in MODEL.JSON "honing". There is no tuning step: the gains come from the same regression as the
 * rest of the model.</p>
 * <ul>
 *     <li>{@code e} is the body-frame pose error {@code [fwd, strafe, turn]} in cm, cm, rad (§8.8.1).</li>
 *     <li>{@code de/dt} is the body-frame velocity error, which for a stationary target is just {@code -v}
 *     (§8.8.2), so the error is never differenced numerically.</li>
 *     <li>The integrator is clamped to {@code integral_limit} and frozen while the previous command was
 *     saturated (§8.8.2).</li>
 *     <li>No feedforward: {@code u_ff} is zero for a stationary target (§8.8.5).</li>
 * </ul>
 * <p>Never finishes on its own. Drive decides when the robot has arrived.</p>
 */
@Config
public class PIDHold extends Task {

    String targetName;
    int target;
    // Target state as a 6x1 column [x, y, h, vx, vy, w], field frame.
    SimpleMatrix targetState;
    Pose2D targetPose;

    // Gains and model terms from MODEL.JSON "honing" (§8.8.1). bEffInv and lambda are loaded but only
    // needed for feedforward, which is not used.
    SimpleMatrix Kp;
    SimpleMatrix Ki;
    SimpleMatrix Kd;
    SimpleMatrix bEffInv;
    SimpleMatrix lambda;
    // Per-axis 3x1 clamp for the integrator, in cm*s, cm*s, rad*s.
    SimpleMatrix integralLimit;

    // Integrator state (3x1). p and d are allocated but unused.
    SimpleMatrix p;
    SimpleMatrix i;
    SimpleMatrix d;

    // Current 6D state (field frame) and the 6D error (body frame): rows 0-2 pose error, rows 3-5 velocity error.
    SimpleMatrix state;
    SimpleMatrix e;

    // 6x6 field -> body rotation R(-h), applied to the pose and velocity halves of the error separately.
    SimpleMatrix R;

    // Last command sent [fwd, strafe, turn]. Also read at the start of the next loop to decide whether to freeze the integrator.
    SimpleMatrix u;

    // Time since the previous run(), used as the integrator's dt.
    ElapsedTime dt;

    /**
     * @param opMode the running opMode; its buildOptimalityEngine() must return true.
     * @param target the target's name as written in MANIFEST.JSON.
     * @throws IllegalStateException if the opMode didn't build an OptimalityEngine.
     * @throws IllegalArgumentException if no target with that name is on the SD card.
     */
    public PIDHold(PeregrineOpMode opMode, String target) {
        this(opMode, new Pose2D(DistanceUnit.CM, 0, 0, AngleUnit.RADIANS, 0));
        targetName = target;
        try {
            this.target = opMode.optimalityEngine.targets.get(targetName);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Invalid target name, no such target present on SD Card.");
        }
        targetState = new SimpleMatrix(new double[][]{opMode.optimalityEngine.getTargetCoords(this.target)}).transpose();
    }

    public PIDHold(PeregrineOpMode opMode, Pose2D target) {
        this.opMode = opMode;
        targetPose = target;
        if (opMode.optimalityEngine == null)
            throw new IllegalStateException("This opMode's buildOptimalityEngine() returns false, but PIDHold needs the OptimalityEngine.");
        targetState = new SimpleMatrix(new double[][]{{target.getX(DistanceUnit.CM),
        target.getY(DistanceUnit.CM), target.getHeading(AngleUnit.RADIANS), 0, 0, 0}}).transpose();

        // Order is fixed by OptimalityEngine.getPIDConstants().
        SimpleMatrix[] pidConstants = opMode.optimalityEngine.getPIDConstants();
        Kp = pidConstants[0];
        Ki = pidConstants[1];
        Kd = pidConstants[2];
        bEffInv = pidConstants[3];
        lambda = pidConstants[4];
        integralLimit = pidConstants[5];

        p = new SimpleMatrix(3, 1);
        i = new SimpleMatrix(3, 1);
        d = new SimpleMatrix(3, 1);

        state = new SimpleMatrix(6, 1);
        e = new SimpleMatrix(6, 1);

        R = new SimpleMatrix(6, 6);

        u = new SimpleMatrix(3, 1);
    }

    @Override
    public boolean run() {
        // The timer starts on the first run, so the first integrator step is ~0.
        // NOTE: Drive only calls run() while honing, so if Drive leaves and re-enters honing, the gap
        // counts as one long dt (bounded by the integrator clamp).
        if (dt == null) dt = new ElapsedTime();
        state = new SimpleMatrix(new double[][]{opMode.localizer.getStateVector()}).transpose();
        // Block-diagonal rotation by -h for [x, y], h and [vx, vy], w: field frame -> body frame (§8.8.1).
        R = new SimpleMatrix(new double[][]{
                {Math.cos(-state.get(2)), -Math.sin(-state.get(2)), 0, 0, 0, 0},
                {Math.sin(-state.get(2)), Math.cos(-state.get(2)), 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0},
                {0, 0, 0, Math.cos(-state.get(2)), -Math.sin(-state.get(2)), 0},
                {0, 0, 0, Math.sin(-state.get(2)), Math.cos(-state.get(2)), 0},
                {0, 0, 0, 0, 0, 1}
        });
        // e = R(-h) * (target - current). Rows 0-2 are the pose error, rows 3-5 are de/dt.
        e = R.mult(targetState.minus(state));
        // Wrap the heading error into [-pi, pi) so the robot turns the short way round.
        e.set(2, mod(e.get(2) + Math.PI, 2*Math.PI) - Math.PI);
        // Anti-windup: only integrate if last loop's command was inside the octahedron |fwd| + |strafe| + |turn| < 1 (§8.8.2).
        if(Math.abs(u.get(0)) + Math.abs(u.get(1)) + Math.abs(u.get(2)) < 1 - 1e-12) {
            for (int x = 0; x < 3; x++) {
                i.set(x,
                        clamp(i.get(x) + e.get(x) * dt.seconds(),
                                -integralLimit.get(x), integralLimit.get(x)));
            }
        }
        // u = Kp*e + Ki*i + Kd*de/dt  (§8.8.2). The gains are full 3x3 matrices, so the axes are coupled (§8.8.3).
        // NOTE: §8.8.2 also asks for traction saturation and an octahedron clip on u. Here the only limit
        // is powerMotors' per-wheel normalisation.
        u = Kp.mult(e.rows(0, 3)).plus(Ki.mult(i)).plus(Kd.mult(e.rows(3, 6)));
        Kinematics.powerMotors(u.get(0), u.get(1), u.get(2), opMode);
        dt.reset();
        return false;
    }

    @Override
    public void end() {
        Kinematics.powerMotors(0, 0, 0, opMode);
    }

    @Override
    public Task reset() {
        return targetName != null ? new PIDHold(opMode, targetName) : new PIDHold(opMode, targetPose);
    }

    // Always-non-negative modulo. Java's % keeps the sign of the dividend.
    private double mod(double a, double b) {
        return ((a % b) + b) % b;
    }
}
