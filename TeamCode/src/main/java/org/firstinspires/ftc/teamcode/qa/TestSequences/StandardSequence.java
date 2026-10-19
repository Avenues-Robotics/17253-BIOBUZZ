package org.firstinspires.ftc.teamcode.qa.TestSequences;

import org.firstinspires.ftc.teamcode.qa.InstantTest;
import org.firstinspires.ftc.teamcode.qa.QAContext;
import org.firstinspires.ftc.teamcode.qa.QATask;
import org.firstinspires.ftc.teamcode.qa.QATest;
import org.firstinspires.ftc.teamcode.qa.QRRoutine;

import java.util.Arrays;
import java.util.List;

/**
 * <h3> This is the Standard Sequence </h3>
 * <p3> It contains one task at the moment that checks voltage</p3>
 * <p2> The next version of this will have drivetrain motor tests (manual).</p2>
  */


public class StandardSequence extends QRRoutine {

    @Override
    public List<QATask> tasks(){
        return Arrays.asList(
                // I think we stick tests in here

                /* FORMAT---> new BlankTest(STUFF, STUFF),
                              new BlankTest(STUFF, STUFF)         */
                new InstantTest("Battery above 12.5 V", QATest.Category.HARDWARE,
                        ctx -> ctx.hardwareMap().voltageSensor.iterator().next().getVoltage() + " V",
                        ctx -> ctx.hardwareMap().voltageSensor.iterator().next().getVoltage() > 12.5
                                ? QATest.Status.PASS : QATest.Status.FAIL)
        );
    }

    @Override
    public void cleanupHardware(QAContext ctx, boolean hardwareBuilt) {
        // Be careful
    }

}
