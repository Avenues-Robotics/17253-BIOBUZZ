package org.firstinspires.ftc.teamcode.qa.TestSequences;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopMovement;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Kinematics;
import org.firstinspires.ftc.teamcode.peregrine.editables.Hardware;
import org.firstinspires.ftc.teamcode.qa.GateQATask;
import org.firstinspires.ftc.teamcode.qa.InstantTest;
import org.firstinspires.ftc.teamcode.qa.ManualCheckTest;
import org.firstinspires.ftc.teamcode.qa.QAContext;
import org.firstinspires.ftc.teamcode.qa.QATask;
import org.firstinspires.ftc.teamcode.qa.QATest;
import org.firstinspires.ftc.teamcode.qa.QRRoutine;

import java.util.ArrayList;
import java.util.List;

/**
 * <h3> This is the Standard Sequence. Please excuse typos, the coder is in mastery </h3>
 * <p>Sequence order:</p>
 * <ol>
 *     <li>Battery voltage (automatic).</li>
 *     <li>Gate: put the robot on blocks, press A.</li>
 *     <li>Drivetrain manual checks (grade each with A = PASS, B = FAIL, X = SKIP, Y = ERROR):
 *     four single-wheel direction checks, three Kinematics patterns (forward, strafe, turn) and
 *     a live TeleopMovement stick check.</li>
 * </ol>
 * <p>The robot MUST be on blocks for the drivetrain section. All motor commands use {@link #POWER}.</p>
 */
public class StandardSequence extends QRRoutine {

    // Motor power for everything that moves the robot. Low on purpose.
    static final double POWER = 0.3;

    static final QATest.Category HW = QATest.Category.HARDWARE;
    static final QATest.Category IN = QATest.Category.INTEGRATED;

    @Override
    public List<QATask> tasks() {
        List<QATask> t = new ArrayList<>();

        // Voltage testing
        t.add(new InstantTest("Battery above 12.5 V", QATest.Category.HARDWARE,
                ctx -> ctx.hardwareMap().voltageSensor.iterator().next().getVoltage() + " V",
                ctx -> ctx.hardwareMap().voltageSensor.iterator().next().getVoltage() > 12.5
                        ? QATest.Status.PASS : QATest.Status.FAIL));

          // Number TWOO
        t.add(gate("Robot on blocks",
                "Put the robot on blocks so all four wheels spin freely, then press A."));

        t.add(wheel("FR", "FRONT-RIGHT"));
        t.add(wheel("FL", "FRONT-LEFT"));
        t.add(wheel("BR", "BACK-RIGHT"));
        t.add(wheel("BL", "BACK-LEFT"));

        t.add(pattern("Kinematics forward", POWER, 0, 0,
                "ALL FOUR wheels should spin FORWARD."));
        t.add(pattern("Kinematics strafe (+x)", 0, POWER, 0,
                "FRONT-LEFT and BACK-RIGHT forward, FRONT-RIGHT and BACK-LEFT backward (strafe to the robot's RIGHT)."));
        t.add(pattern("Kinematics turn (+h)", 0, 0, POWER,
                "RIGHT wheels forward, LEFT wheels backward (turn COUNTER-CLOCKWISE)."));


        final TeleopMovement[] movement = {null};
        t.add(new ManualCheckTest("TeleopMovement stick directions", IN,
                ctx -> "",
                ctx -> {
                    if (movement[0] == null) {
                        ctx.hardware();   // make sure the hardware exists before TeleopMovement uses it
                        movement[0] = new TeleopMovement(ctx.opMode());
                    }
                    movement[0].run();
                },
                "Use gamepad 1's sticks, one at a time, and watch the wheels. Left stick UP: all wheels forward. "
                        + "Left stick RIGHT: strafe-right pattern (FL and BR forward). Right stick RIGHT: "
                        + "clockwise turn (LEFT wheels forward). Small stick movements should be gentle."));

        return t;
    }

    /** Turns every drive motor off after each test. */
    @Override
    public void cleanupHardware(QAContext ctx, boolean hardwareBuilt) {
        if (!hardwareBuilt) return;
        Hardware hw = ctx.hardware();
        hw.FR.setPower(0);
        hw.FL.setPower(0);
        hw.BR.setPower(0);
        hw.BL.setPower(0);
    }

    // ---------------------------------------------------------------------------------------------
    // Builders
    // ---------------------------------------------------------------------------------------------

    /**
     * Waits for the tester to set the robot up and press A. Not a QATest, so it isn't logged, isn't
     * counted in the pass total, has no timeout and can't be skipped with X.
     * Uses aWasPressed() so an A still held from the previous test can't skip the gate.
     */
    static QATask gate(String name, String instruction) {
        return new GateQATask(ctx -> ctx.gamepad1().aWasPressed(),
                ctx -> "GATE: " + name + "\n" + instruction);
    }

    /** Spins one wheel so the tester can check it's the right one, going the right way. */
    static QATest wheel(String field, String description) {
        return new ManualCheckTest("Wheel " + field + " direction", HW,
                ctx -> "",
                ctx -> motor(ctx.hardware(), field).setPower(POWER),
                "Only the " + description + " wheel should spin, turning the way that would drive the robot FORWARD.");
    }

    /** Runs Kinematics.powerMotors with one command so the tester can check the wheel pattern. */
    static QATest pattern(String name, double y, double x, double h, String instruction) {
        return new ManualCheckTest(name, IN,
                ctx -> "",
                ctx -> {
                    ctx.hardware();
                    Kinematics.powerMotors(y, x, h, ctx.opMode());
                },
                instruction);
    }

    static DcMotor motor(Hardware hw, String field) {
        switch (field) {
            case "FR": return hw.FR;
            case "FL": return hw.FL;
            case "BR": return hw.BR;
            default:   return hw.BL;
        }
    }
}