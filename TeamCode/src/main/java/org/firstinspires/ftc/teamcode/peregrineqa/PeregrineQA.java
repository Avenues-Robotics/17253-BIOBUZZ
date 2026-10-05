package org.firstinspires.ftc.teamcode.peregrineqa;

import android.content.Context;
import android.os.Environment;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.ejml.simple.SimpleMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.peregrine.core.annotations.Ends;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.Drive;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.EmptyTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.IdleTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.InstantTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.Localizer;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.Logger;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.PIDHold;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelRaceTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.SeriesTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopMovement;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.TeleopTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.WaitTask;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.WaitUntilTask;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Enums;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Kinematics;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.OptimalityEngine;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;
import org.firstinspires.ftc.teamcode.peregrine.editables.Hardware;
import org.firstinspires.ftc.teamcode.peregrine.editables.RobotParams;
import org.firstinspires.ftc.teamcode.qa.GateQATask;
import org.firstinspires.ftc.teamcode.qa.InstantTest;
import org.firstinspires.ftc.teamcode.qa.ManualCheckTest;
import org.firstinspires.ftc.teamcode.qa.OperatorActionTest;
import org.firstinspires.ftc.teamcode.qa.QAContext;
import org.firstinspires.ftc.teamcode.qa.QATask;
import org.firstinspires.ftc.teamcode.qa.QATest;
import org.firstinspires.ftc.teamcode.qa.QRRoutine;
import org.firstinspires.ftc.teamcode.qa.SimpleTest;
import org.firstinspires.ftc.teamcode.qa.TimeQATask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * <h3>A full check of Peregrine on the real robot.</h3>
 *
 * <p>The tests run in six sections, in this order. The robot has to be in a different place for some
 * of them, and a gate asks you to move it and press A before each of those sections:</p>
 * <ol>
 *     <li><b>Task logic</b> (SOFTWARE): the combiners, helper tasks, ends() and TeleopTask, run with
 *     fake tasks and a fake button. The robot doesn't move.</li>
 *     <li><b>Hardware</b>: the configuration, the Pinpoint and the Localizer.</li>
 *     <li><b>On blocks</b>: each wheel, the Kinematics mixing and TeleopMovement. You grade these by
 *     eye with A (PASS) or B (FAIL).</li>
 *     <li><b>On the floor</b>: odometry directions (you push the robot) and short driven moves.</li>
 *     <li><b>SD card</b>: Logger files, the tables, and Drive/PIDHold construction.</li>
 *     <li><b>Pathing</b>: solve(), and an optional real Drive to a target set in PeregrineQAConfig.</li>
 * </ol>
 *
 * <p>Tests that need something missing (a device, the SD card, the tables) SKIP instead of failing.
 * Press X to skip any test that is waiting on you. Every test gets fresh hardware, so the robot's
 * pose is back at (0, 0), heading 0, at the start of each test.</p>
 *
 * <p>The gates and the pauses between on-floor tests are QATasks rather than QATests, so they don't
 * appear in the log and don't count towards the pass total. They also can't be skipped with X and
 * never time out, so a gate you can't satisfy has to be escaped by stopping the opMode.</p>
 */
@TeleOp(name = "Peregrine QA", group = "QA")
public class PeregrineQA extends QRRoutine {

    static final QATest.Category SW = QATest.Category.SOFTWARE;
    static final QATest.Category HW = QATest.Category.HARDWARE;
    static final QATest.Category IN = QATest.Category.INTEGRATED;

    static final QATest.Status PASS = QATest.Status.PASS;
    static final QATest.Status FAIL = QATest.Status.FAIL;
    static final QATest.Status SKIP = QATest.Status.SKIP;
    static final QATest.Status RUNNING = QATest.Status.RUNNING;

    // Motor power for everything that moves the robot. Low on purpose.
    static final double POWER = 0.3;

    // How long a pause() task waits, in milliseconds. Long enough for the tester to feel the rumble
    // that ends a test and take their hands off the robot.
    static final long SETTLE_MS = 2000;

    @Override
    public List<QATask> tasks() {
        List<QATask> t = new ArrayList<>();
        taskLogicTests(t);
        teleopTaskTests(t);
        hardwareTests(t);
        onBlocksTests(t);
        onFloorTests(t);
        sdCardTests(t);
        pathingTests(t);
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

    // ---------------------------------------------------------------------------------------------
    // 1. Task logic
    // ---------------------------------------------------------------------------------------------

    void taskLogicTests(List<QATask> t) {

        t.add(instant("SeriesTask runs children in order", SW, ctx -> {
            List<String> log = new ArrayList<>();
            Counter a = new Counter("a", 1, log), b = new Counter("b", 2, log), c = new Counter("c", 1, log);
            int loops = runUntilDone(new SeriesTask(a, b, c), 20);
            // The next child starts on the same loop the previous one finishes.
            check(loops == 2, "expected to finish on loop 2, finished on " + loops);
            check(log.equals(Arrays.asList("a.run", "b.run", "b.run", "c.run")), "order was " + log);
            return "";
        }));

        t.add(instant("SeriesTask with one or zero tasks", SW, ctx -> {
            List<String> log = new ArrayList<>();
            int loops = runUntilDone(new SeriesTask(new Counter("a", 3, log)), 20);
            check(loops == 3, "one-task series should finish with its task (loop 3), got " + loops);
            check(runUntilDone(new SeriesTask(), 5) == 1, "empty series should finish on loop 1");
            return "";
        }));

        t.add(instant("ParallelTask waits for all and never re-runs", SW, ctx -> {
            List<String> log = new ArrayList<>();
            Counter a = new Counter("a", 1, log), b = new Counter("b", 3, log), c = new Counter("c", 2, log);
            int loops = runUntilDone(new ParallelTask(a, b, c), 20);
            check(loops == 3, "expected to finish on loop 3, finished on " + loops);
            check(a.runs == 1 && b.runs == 3 && c.runs == 2,
                    "finished children were run again: a=" + a.runs + " b=" + b.runs + " c=" + c.runs);
            check(count(log, "a.end") + count(log, "b.end") + count(log, "c.end") == 0, "something was ended: " + log);
            return "";
        }));

        t.add(instant("ParallelRaceTask ends the losers", SW, ctx -> {
            List<String> log = new ArrayList<>();
            Counter a = new Counter("a", 2, log);
            Forever f = new Forever("f", log);
            Counter g = new Counter("g", 5, log);
            ParallelRaceTask race = new ParallelRaceTask(a, f, g);
            int loops = runUntilDone(race, 20);
            check(loops == 2, "expected to finish on loop 2, finished on " + loops);
            check(a.ends == 0, "the winner was ended");
            check(f.ends == 1 && g.ends == 1, "losers ended f=" + f.ends + " g=" + g.ends + " times, expected 1 each");
            race.end();
            check(f.ends == 1 && g.ends == 1, "end() after the race finished ended the losers again");
            return "";
        }));

        t.add(instant("Single-task race matches its task (#6)", SW, ctx -> {
            List<String> log = new ArrayList<>();
            int loops = runUntilDone(new ParallelRaceTask(new Counter("a", 3, log)), 20);
            check(loops == 3, "should finish when its task does (loop 3), got " + loops);
            check(new ParallelRaceTask(new Counter("a", 3, log)).ends(), "race(ending task).ends() should be true");
            ParallelRaceTask never = new ParallelRaceTask(new Forever("f", log));
            check(!never.ends(), "race(never-ending task).ends() should be false");
            check(!never.reset().ends(), "reset() copy of race(never-ending task).ends() should be false");
            check(runUntilDone(never, 50) == -1, "race(never-ending task) finished");
            check(runUntilDone(new ParallelRaceTask(), 5) == 1, "empty race should finish on loop 1");
            return "";
        }));

        t.add(instant("end() stops only the unfinished children", SW, ctx -> {
            List<String> log = new ArrayList<>();
            Counter a1 = new Counter("a1", 1, log);
            Forever f1 = new Forever("f1", log);
            Task series = new SeriesTask(a1, f1);
            series.run();
            series.run();
            series.end();
            check(f1.ends == 1 && a1.ends == 0, "SeriesTask.end(): a1=" + a1.ends + " f1=" + f1.ends);

            Counter a2 = new Counter("a2", 1, log);
            Forever f2 = new Forever("f2", log);
            Task parallel = new ParallelTask(a2, f2);
            parallel.run();
            parallel.run();
            parallel.end();
            check(f2.ends == 1 && a2.ends == 0, "ParallelTask.end(): a2=" + a2.ends + " f2=" + f2.ends);

            Forever f3 = new Forever("f3", log), f4 = new Forever("f4", log);
            Task race = new ParallelRaceTask(f3, f4);
            race.run();
            race.run();
            race.end();
            check(f3.ends == 1 && f4.ends == 1, "ParallelRaceTask.end(): f3=" + f3.ends + " f4=" + f4.ends);
            return "";
        }));

        t.add(instant("ends() / @Ends is right everywhere", SW, ctx -> {
            List<String> log = new ArrayList<>();
            Counter c = new Counter("c", 1, log);
            Forever f = new Forever("f", log);
            check(c.ends() && !f.ends(), "test tasks: @Ends not read");
            check(new SeriesTask(c, c).ends(), "Series(ends, ends)");
            check(!new SeriesTask(c, f).ends(), "Series(ends, never)");
            check(new ParallelTask(c, c).ends(), "Parallel(ends, ends)");
            check(!new ParallelTask(c, f).ends(), "Parallel(ends, never)");
            check(new ParallelRaceTask(c, f).ends(), "Race(ends, never)");
            check(!new ParallelRaceTask(f, f).ends(), "Race(never, never)");
            check(new SeriesTask(c, new ParallelTask(c, new ParallelRaceTask(c, f))).ends(), "nested tree that ends");
            check(new WaitTask(ctx.opMode(), 10).ends(), "WaitTask");
            check(new WaitUntilTask(() -> true).ends(), "WaitUntilTask");
            check(new InstantTask(() -> { }).ends(), "InstantTask");
            check(new EmptyTask().ends(), "EmptyTask");
            check(!new IdleTask().ends(), "IdleTask");
            check(!new TeleopMovement(ctx.opMode()).ends(), "TeleopMovement");
            check(!new TeleopTask(ctx.opMode(), c, () -> false, false).ends(), "TeleopTask");
            check(Drive.class.isAnnotationPresent(Ends.class), "Drive should be @Ends");
            check(!PIDHold.class.isAnnotationPresent(Ends.class), "PIDHold should not be @Ends");
            check(!Localizer.class.isAnnotationPresent(Ends.class), "Localizer should not be @Ends");
            check(!Logger.class.isAnnotationPresent(Ends.class), "Logger should not be @Ends");
            return "";
        }));

        t.add(instant("EmptyTask IdleTask InstantTask WaitUntilTask", SW, ctx -> {
            check(new EmptyTask().run(), "EmptyTask should finish on its first run");

            IdleTask idle = new IdleTask();
            for (int i = 0; i < 100; i++) check(!idle.run(), "IdleTask finished");
            Task idleCopy = idle.reset();
            check(idleCopy instanceof IdleTask && idleCopy != idle, "IdleTask.reset() should return a new IdleTask");

            final int[] calls = {0};
            InstantTask instant = new InstantTask(() -> calls[0]++);
            check(calls[0] == 0, "InstantTask ran its code when built");
            check(instant.run(), "InstantTask should finish on its first run");
            check(calls[0] == 1, "InstantTask ran its code " + calls[0] + " times");
            check(instant.reset().run() && calls[0] == 2, "InstantTask.reset() copy didn't run the code");

            final boolean[] flag = {false};
            WaitUntilTask waitUntil = new WaitUntilTask(() -> flag[0]);
            check(!waitUntil.run() && !waitUntil.run(), "WaitUntilTask finished while the condition was false");
            flag[0] = true;
            check(waitUntil.run(), "WaitUntilTask didn't finish once the condition was true");
            return "";
        }));

        t.add(instant("WaitTask timing starts on first run", SW, ctx -> {
            WaitTask wait = new WaitTask(ctx.opMode(), 500);
            sleepMs(700);   // time before the first run must not count
            check(!wait.run(), "WaitTask counted the time before its first run");
            long start = System.nanoTime();
            while (!wait.run()) {
                sleepMs(5);
                check(msSince(start) < 2000, "WaitTask(500) still hadn't finished after 2 s");
            }
            double ms = msSince(start);
            check(ms >= 500 && ms < 650, String.format(Locale.US, "WaitTask(500) took %.0f ms", ms));
            check(!wait.reset().run(), "WaitTask.reset() copy wasn't fresh");
            return String.format(Locale.US, "took %.0f ms", ms);
        }));

        t.add(instant("Don't reuse a task object (docs claim)", SW, ctx -> {
            List<String> log = new ArrayList<>();
            Counter shared = new Counter("s", 2, log);
            int loops = runUntilDone(new SeriesTask(shared, shared), 20);
            // The docs say the second use of the same object is already finished: 2 loops, not 4.
            check(loops == 2, "expected the reused object to finish instantly (2 loops), got " + loops);
            return "";
        }));
    }

    // ---------------------------------------------------------------------------------------------
    // 1b. TeleopTask
    // ---------------------------------------------------------------------------------------------

    void teleopTaskTests(List<QATask> t) {

        t.add(instant("TeleopTask hold mode (fake button)", SW, ctx -> {
            List<String> log = new ArrayList<>();
            final boolean[] button = {false};
            TeleopTask tt = new TeleopTask(ctx.opMode(), new Forever("f", log), () -> button[0], false);
            for (int i = 0; i < 3; i++) check(!tt.run(), "TeleopTask.run() returned true");
            check(log.isEmpty(), "task ran before the button was pressed: " + log);

            button[0] = true;
            for (int i = 0; i < 3; i++) tt.run();
            check(count(log, "f.run") == 3, "task should run every loop while held, ran " + count(log, "f.run") + "/3");

            button[0] = false;
            tt.run();
            check(count(log, "f.end") == 1, "releasing should end the task once, log " + log);
            int runsAfterRelease = count(log, "f.run");
            for (int i = 0; i < 3; i++) tt.run();
            check(count(log, "f.run") == runsAfterRelease, "task kept running after release");

            button[0] = true;
            tt.run();
            check(count(log, "f.run") == runsAfterRelease + 1, "a second press didn't start the task again");
            return "";
        }));

        t.add(instant("TeleopTask hold mode when the task finishes while held", SW, ctx -> {
            List<String> log = new ArrayList<>();
            final boolean[] button = {true};
            TeleopTask tt = new TeleopTask(ctx.opMode(), new Counter("c", 2, log), () -> button[0], false);
            for (int i = 0; i < 4; i++) tt.run();
            check(count(log, "c.run") == 2 && count(log, "c.end") == 0,
                    "held for 4 loops: expected 2 runs and 0 ends, log " + log);
            button[0] = false;
            tt.run();
            check(count(log, "c.end") == 0, "a finished task was ended on release");
            button[0] = true;
            tt.run();
            check(count(log, "c.run") == 3, "the next press didn't start a fresh copy");
            return "";
        }));

        t.add(instant("TeleopTask press mode runs to completion", SW, ctx -> {
            List<String> log = new ArrayList<>();
            final boolean[] button = {true};
            TeleopTask tt = new TeleopTask(ctx.opMode(), new Counter("c", 3, log), () -> button[0], true);
            tt.run();
            button[0] = false;
            for (int i = 0; i < 5; i++) tt.run();
            check(count(log, "c.run") == 3 && count(log, "c.end") == 0,
                    "one press: expected 3 runs, 0 ends, log " + log);

            // Pressing again mid-run doesn't cancel it (by design).
            log.clear();
            TeleopTask tt2 = new TeleopTask(ctx.opMode(), new Counter("d", 5, log), () -> button[0], true);
            boolean[] pattern = {true, false, true, false, false, false, false};
            for (boolean p : pattern) { button[0] = p; tt2.run(); }
            check(count(log, "d.run") == 5 && count(log, "d.end") == 0,
                    "press, release, press: expected 5 runs, 0 ends, log " + log);
            return "";
        }));

        t.add(instant("TeleopTask press mode needs an ending task", SW, ctx -> {
            List<String> log = new ArrayList<>();
            expectThrows(IllegalArgumentException.class, "never-ending task",
                    () -> new TeleopTask(ctx.opMode(), new Forever("f", log), () -> false, true));
            expectThrows(IllegalArgumentException.class, "IdleTask",
                    () -> new TeleopTask(ctx.opMode(), new IdleTask(), () -> false, true));
            expectThrows(IllegalArgumentException.class, "race of one never-ending task (#6)",
                    () -> new TeleopTask(ctx.opMode(), new ParallelRaceTask(new Forever("f", log)), () -> false, true));
            // These must be accepted.
            new TeleopTask(ctx.opMode(), new SeriesTask(new WaitTask(ctx.opMode(), 10), new InstantTask(() -> { })), () -> false, true);
            new TeleopTask(ctx.opMode(), new ParallelRaceTask(new Counter("c", 1, log)), () -> false, true);
            new TeleopTask(ctx.opMode(), new WaitUntilTask(() -> true), () -> false, true);
            return "";
        }));

        t.add(instant("TeleopTask.end() ends a running task", SW, ctx -> {
            List<String> log = new ArrayList<>();
            final boolean[] button = {true};
            TeleopTask tt = new TeleopTask(ctx.opMode(), new Forever("f", log), () -> button[0], false);
            tt.run();
            tt.run();
            tt.end();
            check(count(log, "f.end") == 1, "end() while running should end the task once, log " + log);

            log.clear();
            TeleopTask idle = new TeleopTask(ctx.opMode(), new Forever("g", log), () -> false, false);
            idle.run();
            idle.end();
            check(log.isEmpty(), "end() while not running touched the task: " + log);
            return "";
        }));

        t.add(operator("TeleopTask with the real gamepad", IN, ctx -> "",
                new Condition() {
                    final List<String> log = new ArrayList<>();
                    TeleopTask tt;

                    @Override
                    public boolean test(QAContext ctx) {
                        if (tt == null) {
                            tt = new TeleopTask(ctx.opMode(), new Forever("f", log), () -> ctx.gamepad1().a, false);
                        }
                        tt.run();
                        ctx.telem().addData("runs / ends", count(log, "f.run") + " / " + count(log, "f.end"));
                        return count(log, "f.run") >= 10 && count(log, "f.end") == 1;
                    }
                }, "Hold A on gamepad 1 for about a second, then let go.", 20000));
    }

    // ---------------------------------------------------------------------------------------------
    // 2. Hardware
    // ---------------------------------------------------------------------------------------------

    void hardwareTests(List<QATask> t) {

        t.add(instant("Hardware maps every device", HW, ctx -> {
            Hardware hw = ctx.hardware();
            check(hw.odo != null, "odo is null");
            check(hw.FR != null && hw.FL != null && hw.BR != null && hw.BL != null, "a drive motor is null");
            for (DcMotor m : new DcMotor[]{hw.FR, hw.FL, hw.BR, hw.BL}) {
                check(m.getMode() == DcMotor.RunMode.RUN_WITHOUT_ENCODER, "a drive motor isn't RUN_WITHOUT_ENCODER");
                check(m.getZeroPowerBehavior() == DcMotor.ZeroPowerBehavior.BRAKE, "a drive motor isn't BRAKE");
            }
            return "";
        }));

        t.add(new SimpleTest("Pinpoint reports READY", HW,
                ctx -> "status: " + ctx.hardware().odo.getDeviceStatus(),
                ctx -> {
                    ctx.hardware().odo.update();
                    if (ctx.hardware().odo.getDeviceStatus() == GoBildaPinpointDriver.DeviceStatus.READY) return PASS;
                    return ctx.after(5) ? FAIL : RUNNING;
                }, null));

        // NOTE: if the Pinpoint never becomes READY, building the Localizer blocks forever (it has no
        // timeout). If that happens, stop the opMode; the "Pinpoint reports READY" test will have failed.
        t.add(instant("Localizer starts at the origin", HW, ctx -> {
            Localizer loc = ctx.localizer();
            loc.run();
            Pose2D p = loc.getPose();
            check(Math.abs(p.getX(DistanceUnit.CM)) < 1 && Math.abs(p.getY(DistanceUnit.CM)) < 1
                    && Math.abs(p.getHeading(AngleUnit.RADIANS)) < 0.02, "pose was " + pose(loc));
            check(loc.getStateVector().length == 6, "state vector isn't 6 long");
            return pose(loc);
        }));
    }

    // ---------------------------------------------------------------------------------------------
    // 3. On blocks
    // ---------------------------------------------------------------------------------------------

    void onBlocksTests(List<QATask> t) {

        t.add(gate("Robot on blocks",
                "Put the robot on blocks so all four wheels spin freely, then press A."));

        t.add(wheel("FR", "FRONT-RIGHT"));
        t.add(wheel("FL", "FRONT-LEFT"));
        t.add(wheel("BR", "BACK-RIGHT"));
        t.add(wheel("BL", "BACK-LEFT"));

        t.add(instant("Kinematics mixing and normalization", IN, ctx -> {
            if (RobotParams.chassis != Enums.Chassis.MECANUM) throw new QAContext.UnavailableSubsystem("chassis isn't MECANUM");
            Hardware hw = ctx.hardware();
            try {
                Kinematics.powerMotors(0.5, 0.25, 0.1, ctx.opMode());
                checkPowers(hw, 0.35, 0.65, 0.85, 0.15, "powerMotors(0.5, 0.25, 0.1)");
                Kinematics.powerMotors(1, 1, 1, ctx.opMode());
                checkPowers(hw, 1 / 3.0, 1 / 3.0, 1, -1 / 3.0, "powerMotors(1, 1, 1) should be scaled by 1/3");
                Kinematics.powerMotors(0, 0, 0, ctx.opMode());
                checkPowers(hw, 0, 0, 0, 0, "powerMotors(0, 0, 0)");
            } finally {
                Kinematics.powerMotors(0, 0, 0, ctx.opMode());
            }
            return "";
        }));

        t.add(pattern("Kinematics forward", POWER, 0, 0,
                "ALL FOUR wheels should spin FORWARD."));
        t.add(pattern("Kinematics strafe (+x)", 0, POWER, 0,
                "FRONT-LEFT and BACK-RIGHT forward, FRONT-RIGHT and BACK-LEFT backward (strafe to the robot's RIGHT)."));
        t.add(pattern("Kinematics turn (+h)", 0, 0, POWER,
                "RIGHT wheels forward, LEFT wheels backward (turn COUNTER-CLOCKWISE)."));

        t.add(manual("TeleopMovement stick directions", IN,
                new Action2() {
                    TeleopMovement movement;

                    @Override
                    public void run(QAContext ctx) {
                        if (movement == null) {
                            ctx.hardware();
                            movement = new TeleopMovement(ctx.opMode());
                        }
                        movement.run();
                    }
                },
                "Use gamepad 1's sticks, one at a time, and watch the wheels. Left stick UP: all wheels forward. "
                        + "Left stick RIGHT: strafe-right pattern (FL and BR forward). Right stick RIGHT: "
                        + "clockwise turn (LEFT wheels forward). Small stick movements should be gentle."));
    }

    // ---------------------------------------------------------------------------------------------
    // 4. On the floor
    // ---------------------------------------------------------------------------------------------

    void onFloorTests(List<QATask> t) {

        t.add(gate("Robot on the floor",
                "Put the robot on the floor with at least 60 cm clear in front of it and to both sides, then press A."));

        t.add(push("Odometry: push forward", "Push the robot FORWARD by hand about 30 cm.",
                loc -> x(loc) > 20 && Math.abs(y(loc)) < 8));
        t.add(pause("stop pushing forward", SETTLE_MS));
        t.add(push("Odometry: push left", "Push the robot LEFT (sideways) by hand about 30 cm.",
                loc -> y(loc) > 20 && Math.abs(x(loc)) < 8));
        t.add(pause("stop pushing left", SETTLE_MS));
        t.add(push("Odometry: turn counter-clockwise", "Turn the robot about 90 degrees COUNTER-CLOCKWISE by hand.",
                loc -> h(loc) > 1.2));
        t.add(pause("stop turning", SETTLE_MS));
        t.add(push("Odometry: forward velocity", "Push the robot FORWARD briskly.",
                loc -> loc.getVelX(DistanceUnit.CM) > 30));
        t.add(pause("stop pushing forward", SETTLE_MS));

        t.add(driven("Drive forward under power", POWER, 0, 0,
                loc -> x(loc) > 30, loc -> Math.abs(y(loc)) < 10 && Math.abs(h(loc)) < 0.2));
        t.add(pause("let the robot coast to a stop", SETTLE_MS));
        t.add(driven("Strafe right under power", 0, POWER, 0,
                loc -> y(loc) < -30, loc -> Math.abs(x(loc)) < 10 && Math.abs(h(loc)) < 0.2));
        t.add(pause("let the robot coast to a stop", SETTLE_MS));
        t.add(driven("Turn counter-clockwise under power", 0, 0, POWER,
                loc -> h(loc) > 0.8, loc -> Math.abs(x(loc)) < 10 && Math.abs(y(loc)) < 10));
    }

    // ---------------------------------------------------------------------------------------------
    // 5. SD card
    // ---------------------------------------------------------------------------------------------

    void sdCardTests(List<QATask> t) {

        t.add(new SimpleTest("Logger writes a CSV to the SD card", IN,
                ctx -> { File f = newestLog(); return f == null ? "" : f.getName(); },
                new SimpleTest.StepSupplier() {
                    Logger logger;
                    int rows;

                    @Override
                    public QATest.Status run(QAContext ctx) {
                        if (logger == null) {
                            ctx.hardware();
                            ctx.localizer();
                            try {
                                logger = new Logger(ctx.opMode());
                            } catch (IllegalStateException e) {
                                throw new QAContext.UnavailableSubsystem("no usable SD card", e);
                            }
                            logger.addDrivetrainItems();
                        }
                        logger.run();
                        if (++rows < 20) return RUNNING;

                        expectThrows(IllegalStateException.class, "addLogItem() after run()",
                                () -> logger.addLogItem("late", () -> 0));

                        File file = newestLog();
                        check(file != null, "no log file found in the logs folder");
                        List<String> lines = readLines(file);
                        check(lines.size() >= 21, file.getName() + " has " + lines.size() + " lines, expected at least 21");
                        check(lines.get(0).equals("timestamp,FR,FL,BR,BL,x,y,h,x_vel,y_vel,h_vel"),
                                "header was " + lines.get(0));
                        for (int i = 1; i < lines.size(); i++) {
                            check(lines.get(i).split(",").length == 11, "row " + i + " doesn't have 11 columns");
                        }
                        return PASS;
                    }
                }, null));

        t.add(instant("OptimalityEngine loads the tables", IN, ctx -> {
            OptimalityEngine oe = ctx.optimalityEngine();
            check(oe.targetNames.length > 0, "no targets on the card");
            for (int i = 0; i < oe.targetNames.length; i++) {
                check(oe.targetNames[i] != null, "target index " + i + " has no name");
                check(oe.targets.get(oe.targetNames[i]) == i, "targets map is wrong for " + oe.targetNames[i]);
                double[] s = oe.getTargetCoords(i);
                check(s.length == 6, "target " + oe.targetNames[i] + " state isn't 6 long");
                for (double v : s) check(Double.isFinite(v), "target " + oe.targetNames[i] + " has a non-finite state");
            }
            expectThrows(IllegalArgumentException.class, "getTargetCoords(bad index)",
                    () -> oe.getTargetCoords(oe.targetNames.length + 100));
            return "targets: " + joinSpaces(oe.targetNames);
        }));

        t.add(instant("Honing PID constants have the right shape", IN, ctx -> {
            SimpleMatrix[] k = ctx.optimalityEngine().getPIDConstants();
            check(k.length == 6, "expected 6 matrices, got " + k.length);
            String[] names = {"Kp", "Ki", "Kd", "B_eff_inv", "Lambda"};
            for (int i = 0; i < 5; i++) {
                check(k[i].numRows() == 3 && k[i].numCols() == 3,
                        names[i] + " is " + k[i].numRows() + "x" + k[i].numCols() + ", expected 3x3");
            }
            check(k[5].numRows() == 3 && k[5].numCols() == 1, "integral_limit isn't 3x1");
            return "";
        }));

        t.add(instant("Drive and PIDHold reject an unknown target", IN, ctx -> {
            ctx.optimalityEngine();
            expectThrows(IllegalArgumentException.class, "Drive(unknown)",
                    () -> new Drive(ctx.opMode(), "__no_such_target__"));
            expectThrows(IllegalArgumentException.class, "PIDHold(unknown)",
                    () -> new PIDHold(ctx.opMode(), "__no_such_target__"));
            return "";
        }));

        t.add(instant("Drive and PIDHold need the OptimalityEngine", SW, ctx -> {
            // This test never builds the engine, like an opMode whose buildOptimalityEngine() is false.
            check(ctx.opMode().optimalityEngine == null, "engine was already built");
            expectThrows(IllegalStateException.class, "Drive without engine", () -> new Drive(ctx.opMode(), "x"));
            expectThrows(IllegalStateException.class, "PIDHold without engine", () -> new PIDHold(ctx.opMode(), "x"));
            return "";
        }));

        t.add(instant("Drive builds for every target", IN, ctx -> {
            OptimalityEngine oe = ctx.optimalityEngine();
            for (String name : oe.targetNames) {
                Drive d = new Drive(ctx.opMode(), name);
                check(d.ends(), "Drive(" + name + ").ends() is false");
                check(d.reset() instanceof Drive, "Drive(" + name + ").reset() isn't a Drive");
                new PIDHold(ctx.opMode(), name);
            }
            return oe.targetNames.length + " targets";
        }));
    }

    // ---------------------------------------------------------------------------------------------
    // 6. Pathing
    // ---------------------------------------------------------------------------------------------

    void pathingTests(List<QATask> t) {

        t.add(instant("solve() gives a valid command", IN, ctx -> {
            OptimalityEngine oe = ctx.optimalityEngine();
            int target = configuredTarget(oe);
            Localizer loc = ctx.localizer();
            ctx.hardware().odo.setPosition(startPose());
            loc.run();
            double[] u = oe.solve(target);
            check(u.length == 3, "command isn't 3 long");
            double l1 = 0;
            for (double v : u) {
                check(Double.isFinite(v), "command has a non-finite value");
                l1 += Math.abs(v);
            }
            check(l1 == 0 || Math.abs(l1 - 1) < 1e-6, "|fwd| + |strafe| + |turn| should be 0 or 1, was " + l1);
            return String.format(Locale.US, "target %s from %s: [%.2f %.2f %.2f]%s", oe.targetNames[target],
                    pose(loc), u[0], u[1], u[2], l1 == 0 ? " (flat: Drive would use the PID here)" : "");
        }));

        t.add(new SimpleTest("Drive reaches a target", IN,
                ctx -> ctx.opMode().localizer == null ? "" : "ended at " + pose(ctx.opMode().localizer),
                new SimpleTest.StepSupplier() {
                    Drive drive;
                    Logger logger;
                    boolean placed;
                    long start;

                    @Override
                    public QATest.Status run(QAContext ctx) {
                        if (PeregrineQAConfig.driveTarget.isEmpty()) {
                            throw new QAContext.UnavailableSubsystem("set PeregrineQAConfig.driveTarget in Dashboard to run this");
                        }
                        if (drive == null) {
                            configuredTarget(ctx.optimalityEngine());   // clear FAIL message if the name is wrong
                            ctx.localizer();
                            drive = new Drive(ctx.opMode(), PeregrineQAConfig.driveTarget);
                        }
                        if(logger == null) {
                            logger = new Logger(ctx.opMode(), "drive_log");
                            logger.addDrivetrainItems();
                        }
                        if (!placed) {
                            ctx.prompt(String.format(Locale.US,
                                    "Place the robot at x=%.1f cm  y=%.1f cm  heading=%.2f rad (field frame) with room to drive to \"%s\"."
                                            + "\n Press A to GO, X to SKIP. Press STOP if it misbehaves.",
                                    PeregrineQAConfig.startX, PeregrineQAConfig.startY, PeregrineQAConfig.startHeading,
                                    PeregrineQAConfig.driveTarget));
                            if (ctx.gamepad1().x) return SKIP;
                            if (!ctx.gamepad1().a) return RUNNING;
                            placed = true;
                            ctx.hardware().odo.setPosition(startPose());
                            ctx.localizer().run();
                            start = System.nanoTime();
                        }
                        ctx.prompt("Driving to " + PeregrineQAConfig.driveTarget + "...");
                        logger.run();
                        if (drive.run()) return PASS;
                        if (msSince(start) > PeregrineQAConfig.driveTimeoutMs) {
                            drive.end();
                            return FAIL;
                        }
                        return RUNNING;
                    }
                },
                ctx -> Kinematics.powerMotors(0, 0, 0, ctx.opMode()),
                600000));
    }

    // ---------------------------------------------------------------------------------------------
    // Test builders
    // ---------------------------------------------------------------------------------------------

    /** A check that runs once; any failed check() fails the test with its message. */
    interface Check {
        String run(QAContext ctx) throws Exception;
    }

    static QATest instant(String name, QATest.Category category, Check check) {
        final String[] note = {""};
        return new InstantTest(name, category, ctx -> note[0], ctx -> {
            try {
                note[0] = noCommas(check.run(ctx));
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return PASS;
        });
    }

    interface Action2 {
        void run(QAContext ctx);
    }

    interface Condition {
        boolean test(QAContext ctx);
    }

    /** Runs {@code action} every loop until the tester grades it: A PASS, B FAIL, X SKIP, Y ERROR. No timeout. */
    static QATest manual(String name, QATest.Category category, Action2 action, String instruction) {
        return new ManualCheckTest(name, category, ctx -> "", action::run, instruction);
    }

    /** PASS once {@code done} is true; FAIL after {@code timeoutMs}. X SKIP, Y ERROR. */
    static QATest operator(String name, QATest.Category category, OperatorActionTest.NoteSupplier note,
                           Condition done, String instruction, long timeoutMs) {
        return new OperatorActionTest(name, category, note, done::test, instruction, timeoutMs);
    }

    /**
     * Waits for the tester to set the robot up and press A.
     *
     * <p>A GateQATask, not a QATest, so it isn't logged and isn't counted in the pass total. That
     * also means it has no timeout and no X-to-skip: the routine waits here until A is pressed or
     * the opMode is stopped. {@code name} is folded into the prompt because a QATask has no name of
     * its own for telemetry to show.</p>
     *
     * <p>The condition reads aWasPressed() rather than the held {@code a} state. QAContext clears
     * stale button edges when it opens, so an A still held down from grading the previous
     * ManualCheckTest can't satisfy the gate instantly.</p>
     */
    static QATask gate(String name, String instruction) {
        return new GateQATask(ctx -> ctx.gamepad1().aWasPressed(),
                ctx -> "GATE: " + name + "\n" + instruction);
    }

    /**
     * Dead time. Touches no hardware, moves nothing, and finishes once {@code ms} have gone by.
     *
     * <p>Put one after any test the tester moves the robot for. A test only latches its result the
     * instant its condition first becomes true, so the tester is usually still pushing when it ends;
     * without a pause that motion carries into the next test, which zeroes the pose the moment it
     * asks for the localizer and then measures the leftover shove. Because a pause never calls
     * ctx.hardware() or ctx.localizer(), the zeroing happens after the wait, not before it.</p>
     *
     * <p>A TimeQATask, so it isn't logged or counted. The countdown reads the same ctx.testSeconds()
     * clock the task times itself against, so what the tester sees can't drift from the real wait.</p>
     */
    static QATask pause(String reason, long ms) {
        return new TimeQATask(ms, ctx -> String.format(Locale.US,
                "Hands off the robot, let it settle: %s. %.1f s left.",
                reason, Math.max(0, ms / 1000.0 - ctx.testSeconds())));
    }

    /** Spins one wheel so the tester can check it's the right one, going the right way. */
    static QATest wheel(String field, String description) {
        return manual("Wheel " + field + " direction", HW,
                ctx -> motor(ctx.hardware(), field).setPower(POWER),
                "Only the " + description + " wheel should spin, turning the way that would drive the robot FORWARD.");
    }

    /** Runs Kinematics.powerMotors with one command so the tester can check the wheel pattern. */
    static QATest pattern(String name, double y, double x, double h, String instruction) {
        return manual(name, IN,
                ctx -> {
                    ctx.hardware();
                    Kinematics.powerMotors(y, x, h, ctx.opMode());
                },
                instruction);
    }

    interface PoseCondition {
        boolean test(Localizer loc);
    }

    /** Passes once the tester has moved the robot by hand so that the condition is true. */
    static QATest push(String name, String instruction, PoseCondition done) {
        return operator(name, HW,
                ctx -> ctx.opMode().localizer == null ? "" : pose(ctx.opMode().localizer),
                ctx -> done.test(ctx.localizer()),
                instruction, 30000);
    }

    /**
     * Drives with a fixed command until {@code reached} is true (then PASS if {@code straight} is too),
     * or 3 seconds pass (FAIL).
     */
    static QATest driven(String name, double y, double x, double h, PoseCondition reached, PoseCondition straight) {
        return new SimpleTest(name, IN,
                ctx -> ctx.opMode().localizer == null ? "" : "ended at " + pose(ctx.opMode().localizer),
                new SimpleTest.StepSupplier() {
                    long start;

                    @Override
                    public QATest.Status run(QAContext ctx) {
                        Localizer loc = ctx.localizer();
                        if (start == 0) start = System.nanoTime();
                        if (reached.test(loc)) {
                            Kinematics.powerMotors(0, 0, 0, ctx.opMode());
                            return straight.test(loc) ? PASS : FAIL;
                        }
                        if (msSince(start) > 3000) {
                            Kinematics.powerMotors(0, 0, 0, ctx.opMode());
                            return FAIL;
                        }
                        Kinematics.powerMotors(y, x, h, ctx.opMode());
                        return RUNNING;
                    }
                },
                ctx -> Kinematics.powerMotors(0, 0, 0, ctx.opMode()));
    }

    // ---------------------------------------------------------------------------------------------
    // Fake tasks
    // ---------------------------------------------------------------------------------------------

    /** Finishes on its {@code finishAfter}-th run. Records "name.run" and "name.end" in a shared log. */
    @Ends
    static class Counter extends Task {
        final String name;
        final int finishAfter;
        final List<String> log;
        int runs;
        int ends;

        Counter(String name, int finishAfter, List<String> log) {
            this.name = name;
            this.finishAfter = finishAfter;
            this.log = log;
        }

        @Override
        public boolean run() {
            runs++;
            log.add(name + ".run");
            return runs >= finishAfter;
        }

        @Override
        public void end() {
            ends++;
            log.add(name + ".end");
        }

        @Override
        public Task reset() {
            return new Counter(name, finishAfter, log);
        }
    }

    /** Never finishes. Records "name.run" and "name.end" in a shared log. */
    static class Forever extends Task {
        final String name;
        final List<String> log;
        int ends;

        Forever(String name, List<String> log) {
            this.name = name;
            this.log = log;
        }

        @Override
        public boolean run() {
            log.add(name + ".run");
            return false;
        }

        @Override
        public void end() {
            ends++;
            log.add(name + ".end");
        }

        @Override
        public Task reset() {
            return new Forever(name, log);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    /** Fails the test with {@code message}. Commas are replaced, since the message ends up in the CSV log. */
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(noCommas(message));
    }

    static String noCommas(String s) {
        return s == null ? "" : s.replace(',', ';');
    }

    static String joinSpaces(String[] parts) {
        StringBuilder b = new StringBuilder();
        for (String part : parts) b.append(b.length() == 0 ? "" : " ").append(part);
        return b.toString();
    }

    interface Action {
        void run() throws Exception;
    }

    static void expectThrows(Class<? extends Throwable> type, String what, Action action) {
        try {
            action.run();
        } catch (Throwable e) {
            check(type.isInstance(e), what + " threw " + e.getClass().getSimpleName() + " instead of " + type.getSimpleName());
            return;
        }
        throw new AssertionError(noCommas(what + " should have thrown " + type.getSimpleName()));
    }

    /** Runs a task until it finishes. Returns the loop it finished on (1-based), or -1 if it didn't. */
    static int runUntilDone(Task task, int maxLoops) {
        for (int i = 1; i <= maxLoops; i++) {
            if (task.run()) return i;
        }
        return -1;
    }

    static int count(List<String> log, String event) {
        return Collections.frequency(log, event);
    }

    static void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static double msSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1e6;
    }

    static DcMotor motor(Hardware hw, String field) {
        switch (field) {
            case "FR": return hw.FR;
            case "FL": return hw.FL;
            case "BR": return hw.BR;
            default:   return hw.BL;
        }
    }

    static void checkPowers(Hardware hw, double fr, double fl, double br, double bl, String what) {
        double[] expected = {fr, fl, br, bl};
        double[] actual = {hw.FR.getPower(), hw.FL.getPower(), hw.BR.getPower(), hw.BL.getPower()};
        for (int i = 0; i < 4; i++) {
            check(Math.abs(expected[i] - actual[i]) < 0.01, String.format(Locale.US,
                    "%s: expected FR,FL,BR,BL = %.3f,%.3f,%.3f,%.3f but got %.3f,%.3f,%.3f,%.3f", what,
                    fr, fl, br, bl, actual[0], actual[1], actual[2], actual[3]));
        }
    }

    static double x(Localizer loc) { return loc.getPose().getX(DistanceUnit.CM); }
    static double y(Localizer loc) { return loc.getPose().getY(DistanceUnit.CM); }
    static double h(Localizer loc) { return loc.getPose().getHeading(AngleUnit.RADIANS); }

    static String pose(Localizer loc) {
        return String.format(Locale.US, "x=%.1f cm y=%.1f cm h=%.2f rad", x(loc), y(loc), h(loc));
    }

    static Pose2D startPose() {
        return new Pose2D(DistanceUnit.CM, PeregrineQAConfig.startX, PeregrineQAConfig.startY,
                AngleUnit.RADIANS, PeregrineQAConfig.startHeading);
    }

    /** The index of PeregrineQAConfig.driveTarget, or 0 if it's empty. */
    static int configuredTarget(OptimalityEngine oe) {
        if (PeregrineQAConfig.driveTarget.isEmpty()) return 0;
        Integer index = oe.targets.get(PeregrineQAConfig.driveTarget);
        if (index == null) {
            check(false, "PeregrineQAConfig.driveTarget \"" + PeregrineQAConfig.driveTarget
                    + "\" isn't on the card. Targets on the card: " + joinSpaces(oe.targetNames));
        }
        return index;
    }

    /** The newest file in the Logger's folder on the SD card, or null. */
    static File newestLog() {
        Context context = AppUtil.getInstance().getActivity();
        if (context == null) return null;
        for (File dir : context.getExternalFilesDirs(null)) {
            if (dir == null || !Environment.isExternalStorageRemovable(dir)) continue;
            File[] files = new File(dir, "logs").listFiles();
            if (files == null) return null;
            File newest = null;
            for (File f : files) {
                if (newest == null || f.lastModified() > newest.lastModified()) newest = f;
            }
            return newest;
        }
        return null;
    }

    static List<String> readLines(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
        } catch (Exception e) {
            throw new RuntimeException("couldn't read " + file, e);
        }
        return lines;
    }
}
