
package org.firstinspires.ftc.teamcode.qa.TestSequences;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Kinematics;
import org.firstinspires.ftc.teamcode.peregrine.editables.Hardware;

import org.firstinspires.ftc.teamcode.qa.GateQATask;
import org.firstinspires.ftc.teamcode.qa.ManualCheckTest;
import org.firstinspires.ftc.teamcode.qa.QAContext;
import org.firstinspires.ftc.teamcode.qa.QATask;
import org.firstinspires.ftc.teamcode.qa.QATest;
import org.firstinspires.ftc.teamcode.qa.QRRoutine;

import java.util.ArrayList;
import java.util.List;

@TeleOp(name = "Standard QA Sequence")
public class StandardSequence extends QRRoutine {

    // Conservative power for unloaded mechanism checks.
    static final double DRIVE_POWER = 0.20;
    static final double MECHANISM_POWER = 0.15;

    static final QATest.Category HW = QATest.Category.HARDWARE;
    static final QATest.Category IN = QATest.Category.INTEGRATED;

    // PS4 labels and FTC/Logic controller labels:
    // A = Cross, B = Circle, X = Square, Y = Triangle.
    static final String CONTROLS =
            "\n\nCONTROLLER INPUT:"
                    + "\nA / CROSS = PASS / NEXT TEST"
                    + "\nB / CIRCLE = FAIL"
                    + "\nX / SQUARE = SKIP"
                    + "\nY / TRIANGLE = ERROR";

    @Override
    public List<QATask> tasks() {

        List<QATask> t = new ArrayList<>();

        // =========================================================
        // 1. FRAME, STRUCTURE, AND GENERAL HARDWARE
        // =========================================================

        t.add(manual(t, "01 - Chassis plate condition", HW,
                "Inspect every wooden chassis plate. Look for cracks, splitting, "
                        + "delamination, and damaged edges."));

        t.add(manual(t, "02 - Chassis plate warping", HW,
                "Inspect the wooden plates from multiple angles. Look for warping "
                        + "or deformation that could change component alignment."));

        t.add(manual(t, "03 - Structural mounting holes", HW,
                "Inspect holes around screws, motors, and brackets. Check for "
                        + "widening, splitting, or damaged material."));

        t.add(manual(t, "04 - Structural fasteners", HW,
                "Inspect all screws, nuts, and bolts. Check for missing or loose "
                        + "fasteners. Gently check whether mounted components move."));

        t.add(manual(t, "05 - Chassis flex", HW,
                "Apply gentle hand pressure to structural areas. Look for "
                        + "unexpected flexing or movement around heavily loaded components."));

        t.add(manual(t, "06 - Drivetrain mounting integrity", HW,
                "Inspect all four drivetrain motor mounts. Check for loose "
                        + "fasteners, shifting, cracks, and uneven mounting."));

        t.add(manual(t, "07 - Mechanism mounting integrity", HW,
                "Inspect intake, transfer, and outtake mounting points. Check "
                        + "that no mechanism has shifted or loosened."));

        t.add(manual(t, "08 - Wiring clearance", HW,
                "Inspect all wiring. Verify that no wire can contact a wheel, "
                        + "roller, shaft, or other moving component."));

        t.add(manual(t, "09 - Battery retention", HW,
                "Check that the battery is securely mounted and its connector "
                        + "cannot disconnect during normal robot movement."));

        t.add(manual(t, "10 - General mechanism clearance", HW,
                "Inspect the entire robot. Look for collisions, interference, "
                        + "protruding fasteners, and foreign objects."));

        // =========================================================
        // 2. DRIVETRAIN MECHANICAL INSPECTION
        // =========================================================

        t.add(manual(t, "11 - FR wheel free rotation", HW,
                "With the robot powered off and safely lifted, spin the front-right "
                        + "wheel by hand. Check for binding or abnormal resistance."));

        t.add(manual(t, "12 - FL wheel free rotation", HW,
                "With the robot powered off and safely lifted, spin the front-left "
                        + "wheel by hand. Check for binding or abnormal resistance."));

        t.add(manual(t, "13 - BR wheel free rotation", HW,
                "With the robot powered off and safely lifted, spin the back-right "
                        + "wheel by hand. Check for binding or abnormal resistance."));

        t.add(manual(t, "14 - BL wheel free rotation", HW,
                "With the robot powered off and safely lifted, spin the back-left "
                        + "wheel by hand. Check for binding or abnormal resistance."));

        t.add(manual(t, "15 - FR mecanum roller condition", HW,
                "Inspect every roller on the front-right wheel. Check for cracks, "
                        + "missing rollers, debris, and excessive wear."));

        t.add(manual(t, "16 - FL mecanum roller condition", HW,
                "Inspect every roller on the front-left wheel. Check for cracks, "
                        + "missing rollers, debris, and excessive wear."));

        t.add(manual(t, "17 - BR mecanum roller condition", HW,
                "Inspect every roller on the back-right wheel. Check for cracks, "
                        + "missing rollers, debris, and excessive wear."));

        t.add(manual(t, "18 - BL mecanum roller condition", HW,
                "Inspect every roller on the back-left wheel. Check for cracks, "
                        + "missing rollers, debris, and excessive wear."));

        t.add(manual(t, "19 - Wheel wobble inspection", HW,
                "Rotate each wheel slowly. Look for lateral wobble, uneven rotation, "
                        + "or movement at the wheel mounting point."));

        t.add(manual(t, "20 - Wheel surface contamination", HW,
                "Inspect all four wheels for dust, hair, debris, or material that "
                        + "could reduce traction or interfere with roller movement."));

        // =========================================================
        // 3. DRIVETRAIN POWER AND KINEMATICS
        // =========================================================

        t.add(gate("Drivetrain safety preparation",
                "Lift the robot securely so all four wheels are clear of the ground. "
                        + "Keep hands, clothing, and cables away from moving wheels. "
                        + "Press A / CROSS to continue."));

        t.add(poweredMotorTest("21 - FR motor direction", "FR",
                DRIVE_POWER,
                "Only the FRONT-RIGHT wheel should spin. Verify its direction "
                        + "matches the team's forward-wheel convention."));

        t.add(poweredMotorTest("22 - FL motor direction", "FL",
                DRIVE_POWER,
                "Only the FRONT-LEFT wheel should spin. Verify its direction "
                        + "matches the team's forward-wheel convention."));

        t.add(poweredMotorTest("23 - BR motor direction", "BR",
                DRIVE_POWER,
                "Only the BACK-RIGHT wheel should spin. Verify its direction "
                        + "matches the team's forward-wheel convention."));

        t.add(poweredMotorTest("24 - BL motor direction", "BL",
                DRIVE_POWER,
                "Only the BACK-LEFT wheel should spin. Verify its direction "
                        + "matches the team's forward-wheel convention."));

        t.add(drivePattern("25 - Forward kinematics", 1, 0, 0,
                "All four wheels should spin in the forward-driving direction."));

        t.add(drivePattern("26 - Backward kinematics", -1, 0, 0,
                "All four wheels should spin in the reverse-driving direction."));

        t.add(drivePattern("27 - Right strafe kinematics", 0, 1, 0,
                "Expected Kinematics pattern: FR and BL positive; FL and BR negative. "
                        + "Verify the physical robot's wheel directions."));

        t.add(drivePattern("28 - Left strafe kinematics", 0, -1, 0,
                "Expected Kinematics pattern: FR and BL negative; FL and BR positive. "
                        + "Verify the physical robot's wheel directions."));

        t.add(drivePattern("29 - Positive rotation kinematics", 0, 0, 1,
                "Expected Kinematics pattern: FR and BR negative; FL and BL positive. "
                        + "Verify the physical rotation direction matches your convention."));

        t.add(drivePattern("30 - Negative rotation kinematics", 0, 0, -1,
                "Expected Kinematics pattern: FR and BR positive; FL and BL negative. "
                        + "Verify the physical rotation direction matches your convention."));

        t.add(drivePattern("31 - Forward-right diagonal", 1, 1, 0,
                "Verify that the wheel pattern produces forward-right diagonal movement."));

        t.add(drivePattern("32 - Forward-left diagonal", 1, -1, 0,
                "Verify that the wheel pattern produces forward-left diagonal movement."));

        t.add(drivePattern("33 - Drive stop behavior", 0, 0, 0,
                "All four wheels should stop. Check that no motor continues rotating."));

        t.add(manual(t, "34 - Drivetrain noise comparison", HW,
                "Listen to each drivetrain motor during operation. Check for "
                        + "grinding, clicking, scraping, or unusually different sounds."));

        // =========================================================
        // 4. ODOMETRY AND PINPOINT
        // =========================================================

        t.add(gate("Odometry safety preparation",
                "Place the robot on a flat, level surface. Make sure the robot has "
                        + "clear space around it. Do not run powered drivetrain tests here. "
                        + "You will manually move the robot while observing the displayed pose. "
                        + "Press A / CROSS to continue."));

        t.add(odometryCheck("35 - Odometry initialization",
                "Initialize the Pinpoint and observe the displayed X, Y, and heading. "
                        + "Check that the readings are valid and do not jump unexpectedly."));

        t.add(odometryCheck("36 - Odometry forward movement",
                "With drive motors off, carefully move the robot FORWARD by hand. "
                        + "Observe the pose. Verify that the reported displacement matches "
                        + "the robot's forward direction."));

        t.add(odometryCheck("37 - Odometry backward movement",
                "Move the robot BACKWARD by hand. Verify that the reported displacement "
                        + "has the opposite sign from the forward test."));

        t.add(odometryCheck("38 - Odometry right strafe",
                "Move the robot to its RIGHT by hand without rotating it. "
                        + "Verify that the reported displacement matches the configured "
                        + "coordinate convention."));

        t.add(odometryCheck("39 - Odometry left strafe",
                "Move the robot to its LEFT by hand without rotating it. "
                        + "Verify that the reported displacement matches the configured "
                        + "coordinate convention."));

        t.add(odometryCheck("40 - Odometry clockwise rotation",
                "Rotate the robot clockwise by hand. Observe heading change and "
                        + "verify that the sign matches the team's heading convention."));

        t.add(odometryCheck("41 - Odometry counterclockwise rotation",
                "Rotate the robot counterclockwise by hand. Verify that heading "
                        + "changes in the opposite direction from the clockwise test."));

        t.add(odometryCheck("42 - Odometry 90-degree rotation",
                "Rotate the robot approximately 90 degrees. Compare the displayed "
                        + "heading change with the physical rotation."));

        t.add(odometryCheck("43 - Odometry 180-degree rotation",
                "Rotate the robot approximately 180 degrees. Check for heading "
                        + "accuracy and unexpected discontinuities."));

        t.add(odometryCheck("44 - Odometry return-to-origin",
                "Move the robot away from its starting position, then carefully "
                        + "return it to the same physical position and heading. Check "
                        + "whether the reported pose returns close to its starting value."));

        t.add(odometryCheck("45 - Odometry diagonal movement",
                "Move the robot diagonally without rotating it. Verify that both "
                        + "position coordinates change consistently."));

        t.add(odometryCheck("46 - Odometry translation-heading stability",
                "Move the robot forward while keeping its physical heading unchanged. "
                        + "Check that the reported heading remains stable."));

        t.add(odometryCheck("47 - Odometry repeatability",
                "Repeat the same short movement several times, returning to the "
                        + "starting point each time. Observe whether the reported movement "
                        + "is consistent."));

        t.add(manual(t, "48 - Odometry pod contact and mounting", HW,
                "Inspect both odometry pods. Verify secure mounting, free wheel "
                        + "rotation, proper floor contact, spring movement if present, "
                        + "and no interference with the chassis."));

        // =========================================================
        // 5. INTAKE
        // Intake motors are not currently mapped in Hardware.java.
        // These are physical inspection and manual game-piece tests.
        // =========================================================

        t.add(manual(t, "49 - Intake vector wheel condition", HW,
                "Inspect all vector wheels. Check for damaged surfaces, missing "
                        + "components, uneven wear, and debris."));

        t.add(manual(t, "50 - Intake roller condition", HW,
                "Inspect every intake roller. Check for cracks, deformation, "
                        + "loose mounting, and uneven wear."));

        t.add(manual(t, "51 - Intake shaft free rotation", HW,
                "With the robot powered off, rotate accessible intake shafts "
                        + "by hand. Check for binding or abnormal resistance."));

        t.add(manual(t, "52 - Intake roller spacing", HW,
                "Inspect the spacing between intake rollers and vector wheels. "
                        + "Check for inconsistent gaps or areas where POLLEN could become trapped."));

        t.add(manual(t, "53 - Intake alignment", HW,
                "Inspect intake alignment relative to the robot frame. Check "
                        + "that the mechanism has not shifted or twisted."));

        t.add(manual(t, "54 - Intake mouth clearance", HW,
                "Inspect the intake entrance. Check for protruding screws, sharp "
                        + "edges, gaps, or obstructions that could catch POLLEN."));

        t.add(manual(t, "55 - Intake center acquisition", IN,
                "Place a POLLEN piece directly in front of the center of the intake. "
                        + "Manually observe whether the geometry guides it toward the transfer."));

        t.add(manual(t, "56 - Intake left-side acquisition", IN,
                "Place a POLLEN piece at the left edge of the intake. Observe "
                        + "whether the vector wheels guide it inward without trapping it."));

        t.add(manual(t, "57 - Intake right-side acquisition", IN,
                "Place a POLLEN piece at the right edge of the intake. Observe "
                        + "whether the vector wheels guide it inward without trapping it."));

        t.add(manual(t, "58 - Intake orientation sensitivity", IN,
                "Test POLLEN acquisition with several different starting orientations. "
                        + "Record any orientation that consistently causes a jam or miss."));

        t.add(manual(t, "59 - Intake consecutive-piece clearance", IN,
                "Manually introduce multiple POLLEN pieces in sequence. Check "
                        + "whether one piece blocks the next or creates a jam."));

        t.add(manual(t, "60 - Intake structural rigidity", HW,
                "Gently check intake brackets and mounts. Look for flex, vibration "
                        + "points, loose fasteners, or movement relative to the chassis."));

        // =========================================================
        // 6. TRANSFER
        // =========================================================

        t.add(manual(t, "61 - Transfer rail straightness", HW,
                "Inspect the entire transfer rail. Check for bends, twists, "
                        + "misalignment, or uneven surfaces."));

        t.add(manual(t, "62 - Transfer rail clearance", HW,
                "Check the available clearance throughout the transfer. Look "
                        + "for narrow points where POLLEN could become trapped."));

        t.add(manual(t, "63 - Transfer rail joints and seams", HW,
                "Inspect every joint, seam, screw, and transition along the rail. "
                        + "Check for edges that could catch a game piece."));

        t.add(manual(t, "64 - Transfer roller surface inspection", HW,
                "Inspect transfer rollers for wear, damage, debris, and uneven "
                        + "contact surfaces."));

        t.add(manual(t, "65 - Transfer shaft and bearing play", HW,
                "Check accessible shafts and bearings for excessive lateral "
                        + "movement, looseness, or abnormal resistance."));

        t.add(manual(t, "66 - Single-piece transfer passage", IN,
                "With motors off, manually guide one POLLEN piece through the "
                        + "entire transfer path. Identify any catching points or changes "
                        + "in orientation."));

        t.add(manual(t, "67 - Multiple-piece transfer passage", IN,
                "Manually guide multiple POLLEN pieces through the transfer in "
                        + "sequence. Check for interference between pieces."));

        t.add(manual(t, "68 - Intake-to-transfer handoff", IN,
                "Manually guide POLLEN from the intake entrance through the "
                        + "intake-to-transfer handoff. Check for gaps, catching points, "
                        + "and unnecessary resistance."));

        // =========================================================
        // 7. OUTTAKE AND SHOOTING MECHANISM
        // =========================================================

        t.add(manual(t, "69 - Flywheel mounting inspection", HW,
                "Inspect the flywheel motor and mounting structure. Check for "
                        + "loose fasteners, shifting, and structural flex."));

        t.add(manual(t, "70 - Flywheel free rotation", HW,
                "With the robot powered off, check accessible flywheel rotation "
                        + "by hand. Follow team safety procedures and do not force the shaft. "
                        + "Check for scraping or abnormal resistance."));

        t.add(poweredMotorTest("71 - Flywheel low-power operation", "FLYWHEEL",
                MECHANISM_POWER,
                "The flywheel should rotate smoothly at low power. Keep hands, "
                        + "clothing, and objects clear. Check for vibration, scraping, "
                        + "and abnormal noise."));

        t.add(manual(t, "72 - Declination mechanism inspection", HW,
                "Inspect the declination motor mounting, linkage, and moving "
                        + "assembly. Check for binding, excessive play, and loose hardware."));

        t.add(manual(t, "73 - Blocker mechanism inspection", HW,
                "Inspect the blocker servo, linkage, and mounting. Check for "
                        + "binding, loose connections, and interference with the POLLEN path."));

        t.add(manual(t, "74 - Outtake exit alignment", HW,
                "Inspect the shooting exit and surrounding structure. Check "
                        + "that the exit is clear, secure, and aligned with the intended "
                        + "shooting direction."));

        // =========================================================
        // 8. FULL-SYSTEM INTEGRATION
        // =========================================================

        t.add(gate("Full-system inspection preparation",
                "Power down the robot before manually moving game pieces. "
                        + "Keep hands clear of all powered mechanisms. Prepare one POLLEN "
                        + "piece and inspect the complete path from intake to outtake. "
                        + "Press A / CROSS to continue."));

        t.add(manual(t, "75 - Complete POLLEN path inspection", IN,
                "Manually trace one POLLEN piece from intake, through the transfer, "
                        + "and into the outtake. Verify that every handoff is clear and "
                        + "that no mechanism obstructs the next stage."));

        return t;
    }

    // =============================================================
    // TEST HELPERS
    // =============================================================

    static QATest manual(
            List<QATask> tasks,
            String name,
            QATest.Category category,
            String instruction) {

        return new ManualCheckTest(
                name,
                category,
                ctx -> "",
                ctx -> {},
                instruction + CONTROLS
        );
    }

    static QATask gate(String name, String instruction) {

        return new GateQATask(
                ctx -> ctx.gamepad1().aWasPressed(),
                ctx -> "GATE: " + name
                        + "\n\n" + instruction
                        + "\nA / CROSS = CONTINUE"
                        + "\nB / CIRCLE = NOT USED"
                        + "\nX / SQUARE = NOT USED"
                        + "\nY / TRIANGLE = NOT USED"
        );
    }

    static QATest poweredMotorTest(
            String name,
            String motorName,
            double power,
            String instruction) {

        return new ManualCheckTest(
                name,
                HW,
                ctx -> "",
                ctx -> {
                    Hardware hw = ctx.hardware();

                    switch (motorName) {
                        case "FR":
                            hw.FR.setPower(power);
                            break;
                        case "FL":
                            hw.FL.setPower(power);
                            break;
                        case "BR":
                            hw.BR.setPower(power);
                            break;
                        case "BL":
                            hw.BL.setPower(power);
                            break;
                        case "FLYWHEEL":
                            hw.flywheel.setPower(power);
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Unknown motor: " + motorName);
                    }
                },
                instruction + CONTROLS
        );
    }

    static QATest drivePattern(
            String name,
            double forward,
            double strafe,
            double turn,
            String instruction) {

        return new ManualCheckTest(
                name,
                IN,
                ctx -> "",
                ctx -> {
                    ctx.hardware();
                    Kinematics.powerMotors(
                            forward * DRIVE_POWER,
                            strafe * DRIVE_POWER,
                            turn * DRIVE_POWER,
                            ctx.opMode()
                    );
                },
                instruction + CONTROLS
        );
    }

    static QATest odometryCheck(
            String name,
            String instruction) {

        return new ManualCheckTest(
                name,
                IN,
                ctx -> "",
                ctx -> {
                    Pose2D pose = ctx.localizer().getPose();

                    double x = pose.getX(DistanceUnit.CM);
                    double y = pose.getY(DistanceUnit.CM);
                    double heading = pose.getHeading(AngleUnit.DEGREES);

                    ctx.telem().addData("ODOMETRY X", "%.2f cm", x);
                    ctx.telem().addData("ODOMETRY Y", "%.2f cm", y);
                    ctx.telem().addData("HEADING", "%.2f degrees", heading);
                },
                instruction
                        + "\n\nMove the robot slowly and observe the readings."
                        + CONTROLS
        );
    }

    // =============================================================
    // HARDWARE CLEANUP
    // =============================================================

    @Override
    public void cleanupHardware(QAContext ctx, boolean hardwareBuilt) {

        if (!hardwareBuilt) return;

        Hardware hw = ctx.hardware();

        // Always stop drivetrain.
        hw.FR.setPower(0);
        hw.FL.setPower(0);
        hw.BR.setPower(0);
        hw.BL.setPower(0);

        // Stop launcher and transfer.
        hw.flywheel.setPower(0);
        hw.transferR.setPower(0);
        hw.transferL.setPower(0);

        // Do not move declination or blocker automatically.
    }
}
