package org.firstinspires.ftc.teamcode.qa.TestSequences;

import org.firstinspires.ftc.teamcode.qa.QRRoutine;

import java.util.Arrays;

public class StandardSequence extends QRRoutine {

    @Override
    public List<StandardTests> tests(){
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

}
