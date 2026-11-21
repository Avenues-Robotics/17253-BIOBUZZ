---
title: QA testing
nav_order: 9
---

# QA testing
{: .no_toc }

1. TOC
{:toc}

The `qa` package is a small framework for **robot check-lists**: a list of tests you run before a
match or after a repair, each ending in PASS, FAIL, SKIP or ERROR, with the results saved to a file.
Some tests check things automatically; others ask the person at the Driver Station to watch the robot
or do something to it.

## Writing a QA routine

A routine is an opMode that extends `QRRoutine`. Give it a list of tests and a way to make the robot
safe between them:

```java
@TeleOp(group = "QA")
public class PreMatchCheck extends QRRoutine {

    @Override
    public List<QATest> tests() {
        return Arrays.asList(
                new InstantTest("Battery above 12.5 V", QATest.Category.HARDWARE,
                        ctx -> ctx.hardwareMap().voltageSensor.iterator().next().getVoltage() + " V",
                        ctx -> ctx.hardwareMap().voltageSensor.iterator().next().getVoltage() > 12.5
                                ? QATest.Status.PASS : QATest.Status.FAIL),

                new ManualCheckTest("Intake spins inward", QATest.Category.HARDWARE,
                        ctx -> "",
                        ctx -> ctx.hardware().intake.setPower(0.5),
                        "Check the intake is pulling inward"),

                new OperatorActionTest("Lift limit switch", QATest.Category.HARDWARE,
                        ctx -> "",
                        ctx -> ctx.hardware().liftSwitch.isPressed(),
                        "Press the lift's limit switch",
                        10000)
        );
    }

    // Called after every test. Turn everything off.
    @Override
    public void cleanupHardware(QAContext ctx, boolean hardwareBuilt) {
        if (!hardwareBuilt) return;
        ctx.hardware().intake.setPower(0);
        ctx.hardware().FR.setPower(0);
        ctx.hardware().FL.setPower(0);
        ctx.hardware().BR.setPower(0);
        ctx.hardware().BL.setPower(0);
    }
}
```

Each test takes a **name** and a **category** (`SOFTWARE`, `HARDWARE` or `INTEGRATED`), then one or
more lambdas that receive a `QAContext` called `ctx`. The first lambda is usually the **note**: text
written next to the result in the log, such as a measured value. Return `""` if you have nothing to
add.

## Running it

1. Connect to FTC Dashboard and, in the **Configuration** panel under `QAConfig`, set `tester` to your
   name and `logName` to a name for this run (for example `pre-match-states-1`).
   The routine refuses to start while `logName` is still `Unnamed Log`.
2. On the Driver Station, select the routine, then INIT and START.
3. Follow the instructions on telemetry. The current test is shown at the top as
   `CATEGORY [STATUS]: name`.
4. At the end you'll see how many tests passed, plus a list of the ones that didn't. Press STOP when
   you've read it.

## The kinds of test

| Test | What it does | How it finishes |
| --- | --- | --- |
| `InstantTest(name, category, note, step)` | Runs `step` once. | With whatever status `step` returns. |
| `SimpleTest(name, category, note, step, cleanup[, timeoutMs])` | Runs `step` every loop. | When `step` returns anything other than `RUNNING`. `note` and `cleanup` may be `null`. Default timeout 15 s. |
| `ManualCheckTest(name, category, note, step, instruction)` | Shows the instruction and runs `step` every loop so the tester can watch. | When the tester presses **A** (PASS), **B** (FAIL), **X** (SKIP) or **Y** (ERROR) on gamepad 1. No timeout. |
| `OperatorActionTest(name, category, note, step, instruction, timeoutMs)` | Shows the instruction and runs `step` every loop. | PASS as soon as `step` returns `true`; FAIL if `timeoutMs` passes first. The tester can press **X** (SKIP) or **Y** (ERROR). |

A `SimpleTest` that needs a few loops, such as checking a motor's encoder counts up:

```java
new SimpleTest("Lift encoder", QATest.Category.HARDWARE,
        ctx -> "ticks: " + ctx.hardware().lift.getCurrentPosition(),
        ctx -> {
            ctx.hardware().lift.setPower(0.3);
            if (ctx.hardware().lift.getCurrentPosition() > 100) return QATest.Status.PASS;
            if (ctx.after(2)) return QATest.Status.FAIL;   // give up after 2 s
            return QATest.Status.RUNNING;
        },
        ctx -> ctx.hardware().lift.setPower(0))
```

For anything more involved, extend `QATest` yourself and implement `step(QAContext ctx)`: set
`status` and return `true` when done.

## What a test can use: `QAContext`

| Method | What it gives you |
| --- | --- |
| `ctx.hardware()` | Your `Hardware`, built the first time a test asks for it. |
| `ctx.hardwareMap()` | The FTC `hardwareMap`, for devices that aren't in `Hardware`. |
| `ctx.localizer()` | A `Localizer` starting at (0, 0), heading 0. Once built, it's updated every loop. |
| `ctx.optimalityEngine()` | The pathing tables from the SD card. |
| `ctx.globalVariables()` | A `GlobalVariables` object. |
| `ctx.opMode()` | A stand-in opMode for building Peregrine tasks, for example `new WaitTask(ctx.opMode(), 500)`. |
| `ctx.telem()` | Telemetry to the Driver Station and Dashboard. |
| `ctx.prompt("…")` | Shows an instruction to the tester. |
| `ctx.gamepad1()`, `ctx.gamepad2()` | The gamepads. |
| `ctx.testSeconds()`, `ctx.after(seconds)` | Time since this test started. |

Every test gets a fresh context, so each test starts from a clean slate.

## How results are decided

Besides the status a test sets itself, `QRRoutine` decides the result when:

| Situation | Status | Note added to the log |
| --- | --- | --- |
| The test runs longer than its timeout | FAIL | `TIMED OUT` |
| `ctx.hardware()` (or another subsystem) can't be built, e.g. a device isn't in the configuration | SKIP | the error |
| The test throws an `AssertionError` | FAIL | the error |
| The test throws anything else | ERROR | the error |
| STOP is pressed mid-test | SKIP | `STOPPED` |

To fail a test from inside a lambda, `throw new AssertionError("reason")`. Java's `assert` keyword is
switched off on Android, so use `throw` instead.

A device that can't be built makes a test SKIP rather than FAIL, so a single unplugged device doesn't
hide the results of everything else.

## The log file

Results are saved on the Control Hub's **internal storage** (not the SD card), at
`/sdcard/FIRST/qa/<logName>.csv`. To copy it to your computer:

```bash
adb pull /sdcard/FIRST/qa
```

Each run adds a line naming the log and the tester, a `Name,Category,Status,Note` header, and one row
per test. Running again with the same `logName` adds to the end of the same file.
