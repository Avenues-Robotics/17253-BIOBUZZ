
package org.firstinspires.ftc.teamcode.qa.TestSequences;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
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
import org.firstinspires.ftc.teamcode.qa.SimpleTest;

import java.util.ArrayList;
import java.util.List;

@TeleOp
public class StandardSequence extends QRRoutine {

    static final double POWER = 0.3;
    static final double ODO_POWER = 0.2;
    static final double TARGET_DISTANCE = 10.0;

    static final QATest.Category HW = QATest.Category.HARDWARE;
    static final QATest.Category IN = QATest.Category.INTEGRATED;

    @Override
    public List<QATask> tasks() {
        List<QATask> t = new ArrayList<>();

        // Battery voltage
        t.add(new InstantTest("Battery above 12.5 V", HW,
                ctx -> ctx.hardwareMap().voltageSensor.iterator().next().getVoltage() + " V",
                ctx -> ctx.hardwareMap().voltageSensor.iterator().next().getVoltage() > 12.5
                        ? QATest.Status.PASS : QATest.Status.FAIL));

        // Put robot on blocks for drivetrain checks
        t.add(gate("Robot on blocks",
                "Put the robot on blocks so all four wheels spin freely, then press A."));

        // Individual wheel checks
        t.add(wheel("FR", "FRONT-RIGHT"));
        t.add(wheel("FL", "FRONT-LEFT"));
        t.add(wheel("BR", "BACK-RIGHT"));
        t.add(wheel("BL", "BACK-LEFT"));

        // Kinematics checks
        t.add(pattern("Kinematics forward", POWER, 0, 0,
                "ALL FOUR wheels should spin FORWARD."));

        t.add(pattern("Kinematics strafe (+x)", 0, POWER, 0,
                "FRONT-LEFT and BACK-RIGHT forward, FRONT-RIGHT and BACK-LEFT backward (strafe to the robot's RIGHT)."));

        t.add(pattern("Kinematics turn (+h)", 0, 0, POWER,
                "RIGHT wheels forward, LEFT wheels backward (turn COUNTER-CLOCKWISE)."));

        // Teleop movement check
        final TeleopMovement[] movement = {null};

        t.add(new ManualCheckTest("TeleopMovement stick directions", IN,
                ctx -> "",
                ctx -> {
                    if (movement[0] == null) {
                        ctx.hardware();
                        movement[0] = new TeleopMovement(ctx.opMode());
                    }
                    movement[0].run();
                },
                "Use gamepad 1's sticks, one at a time, and watch the wheels. "
                        + "Left stick UP: all wheels forward. "
                        + "Left stick RIGHT: strafe-right pattern (FL and BR forward). "
                        + "Right stick RIGHT: clockwise turn (LEFT wheels forward). "
                        + "Small stick movements should be gentle."));

        // Move robot off blocks for odometry tests
        t.add(gate("Prepare for odometry",
                "Remove the robot from the blocks and place it on a flat surface. "
                        + "Make sure there is at least 10 cm of clear space in every direction, "
                        + "then press A."));

        // Pinpoint movement checks
        t.add(odometryTest("Odometry forward", 1, 0));
        t.add(odometryTest("Odometry backward", -1, 0));
        t.add(odometryTest("Odometry strafe right", 0, 1));
        t.add(odometryTest("Odometry strafe left", 0, -1));

        return t;
    }

    @Override
    public void cleanupHardware(QAContext ctx, boolean hardwareBuilt) {
        if (!hardwareBuilt) return;

        Hardware hw = ctx.hardware();

        hw.FR.setPower(0);
        hw.FL.setPower(0);
        hw.BR.setPower(0);
        hw.BL.setPower(0);
    }

    static QATask gate(String name, String instruction) {
        return new GateQATask(
                ctx -> ctx.gamepad1().aWasPressed(),
                ctx -> "GATE: " + name + "\n" + instruction
        );
    }

    static QATest wheel(String field, String description) {
        return new ManualCheckTest("Wheel " + field + " direction", HW,
                ctx -> "",
                ctx -> motor(ctx.hardware(), field).setPower(POWER),
                "Only the " + description
                        + " wheel should spin, turning the way that would drive the robot FORWARD.");
    }

    static QATest pattern(String name, double y, double x, double h, String instruction) {
        return new ManualCheckTest(name, IN,
                ctx -> "",
                ctx -> {
                    ctx.hardware();
                    Kinematics.powerMotors(y, x, h, ctx.opMode());
                },
                instruction);
    }

    static QATest odometryTest(String name, double forwardDirection,
                               double rightDirection) {

        final double[] startX = {0};
        final double[] startY = {0};
        final double[] startHeading = {0};
        final boolean[] initialized = {false};

        return new SimpleTest(name, IN,
                ctx -> "Target: " + TARGET_DISTANCE + " cm",
                ctx -> {
                    Pose2D pose = ctx.opMode().localizer.getPose();

                    double x = pose.getX(DistanceUnit.CM);
                    double y = pose.getY(DistanceUnit.CM);
                    double heading = pose.getHeading(AngleUnit.RADIANS);

                    if (!initialized[0]) {
                        startX[0] = x;
                        startY[0] = y;
                        startHeading[0] = heading;
                        initialized[0] = true;

                        setMovement(ctx, forwardDirection, rightDirection);
                        return QATest.Status.RUNNING;
                    }

                    double dx = x - startX[0];
                    double dy = y - startY[0];

                    // Convert field-frame displacement into robot-relative movement.
                    double forward = dx * Math.sin(startHeading[0])
                            + dy * Math.cos(startHeading[0]);

                    double right = dx * Math.cos(startHeading[0])
                            - dy * Math.sin(startHeading[0]);

                    double progress = forward * forwardDirection
                            + right * rightDirection;

                    if (progress >= TARGET_DISTANCE) {
                        stopDrive(ctx);
                        return QATest.Status.PASS;
                    }

                    return QATest.Status.RUNNING;
                },
                ctx -> stopDrive(ctx),
                20000
        );
    }

    static void setMovement(QAContext ctx, double forward, double right) {
        Kinematics.powerMotors(
                forward * ODO_POWER,
                right * ODO_POWER,
                0,
                ctx.opMode()
        );
    }

    static void stopDrive(QAContext ctx) {
        Hardware hw = ctx.hardware();
        hw.FR.setPower(0);
        hw.FL.setPower(0);
        hw.BR.setPower(0);
        hw.BL.setPower(0);
    }

    static DcMotor motor(Hardware hw, String field) {
        switch (field) {
            case "FR": return hw.FR;
            case "FL": return hw.FL;
            case "BR": return hw.BR;
            default: return hw.BL;
        }
    }
}
