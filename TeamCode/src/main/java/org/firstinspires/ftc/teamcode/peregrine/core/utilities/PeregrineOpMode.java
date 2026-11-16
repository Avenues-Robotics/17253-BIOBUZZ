package org.firstinspires.ftc.teamcode.peregrine.core.utilities;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.Localizer;
import org.firstinspires.ftc.teamcode.peregrine.core.tasks.ParallelRaceTask;
import org.firstinspires.ftc.teamcode.peregrine.editables.GlobalVariables;
import org.firstinspires.ftc.teamcode.peregrine.editables.Hardware;

/**
 * <h3>PeregrineOpMode is the basic class that all opModes extend from, replacing LinearOpMode in
 * the normal FTC SDK.</h3>
 *
 * <p>Every autonomous and teleop extends PeregrineOpMode directly and adds the usual
 * {@code @Autonomous} or {@code @TeleOp} annotation. At INIT it builds the hardware, localizer,
 * global variables, telemetry and (unless {@link #buildOptimalityEngine()} is overridden to return
 * false) the OptimalityEngine, then asks you for your task tree with {@link #defineTasks()}. After
 * START it runs that tree, alongside the Localizer, once per loop until the tree finishes or the
 * opMode is stopped.</p>
 *
 * <p>It also provides a universal interface that tasks use, through their {@code opMode} field, to
 * access the robot's hardware, its position, telemetry, the gamepads, and any global variables that
 * you may choose to add to the GlobalVariables class.</p>
 */

public abstract class PeregrineOpMode extends LinearOpMode {

    /**This is the hardware object that contains all maps to robot hardware and that can be accessed through the opMode by any other class. See editables/Hardware.java.*/
    public Hardware hardware;

    /**This is the localizer object that keeps track of the robot's position using the goBILDA Pinpoint. It is a specialized task that runs alongside every opMode's task tree.*/
    public Localizer localizer;

    /**This is the global variables object that contains every variable you want to share between tasks. It is in the editables package so you can add your own fields.*/
    public GlobalVariables globalVariables;

    /**
     * This is the telemetry object. Anything added to it is shown both on the Driver Station and on FTC
     * Dashboard (192.168.43.1:8080/dash on your robot's network). It is updated once per loop for you.
     */
    public Telemetry telem;

    /**
     * Reads the SD-card value tables and drivetrain model and turns them into drive commands. It is
     * built at INIT unless {@link #buildOptimalityEngine()} returns false, in which case it stays null.
     * If the card or tables are missing or invalid, building it throws and the opMode stops, see
     * OptimalityEngine.
     */
    public OptimalityEngine optimalityEngine;

    // The root of the task tree: the Localizer raced against the tree from defineTasks().
    Task tree;

    /**
     * This function should return the pose of the robot when init is pressed, in the field frame the
     * tables were solved in. Everything the Localizer reports afterwards is relative to it.
     * @return The pose of the robot when init is pressed
     */
    public abstract Pose2D startingPose();

    //This is the regular opMode function, being mapped to those below
    public void runOpMode() {

        // Construction order matters: Localizer needs hardware and telem, and defineTasks() comes last
        // because tasks such as Drive use the other objects in their constructors.
        telem = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        hardware = new Hardware(this);
        // Blocks until the Pinpoint reports READY, then sets its pose to startingPose().
        localizer = new Localizer(this, startingPose());
        globalVariables = new GlobalVariables();
        if(buildOptimalityEngine()) optimalityEngine = new OptimalityEngine(this);

        // The Localizer never finishes, so this race finishes exactly when your task tree does.
        tree = new ParallelRaceTask(localizer, defineTasks());
        initStart();

        while(opModeInInit()) {
            initLoop();
            telem.update();
        }

        waitForStart();

        mainStart();

        // Tick the task tree once per loop until it finishes (the tree from defineTasks() is done) or
        // the opMode is stopped. telem goes to both the Driver Station and FTC Dashboard.
        while(opModeIsActive() && !tree.run()) {
            mainLoop();
            telem.update();
            // Stopped during this loop: let unfinished tasks put the robot in a safe state.
            if(!opModeIsActive()) {
                tree.end();
            }
        }

        if(optimalityEngine != null) optimalityEngine.closeReaders();
        end();
    }

    /**
     * Builds and returns the task tree for this opMode. It is called once during INIT, after the
     * hardware, localizer, global variables and OptimalityEngine have been built, so any task
     * constructors also run during INIT. When the returned task finishes, the opMode ends.
     * @return the root task to run after START
     */
    public abstract Task defineTasks();

    /**Is run once during init, right after defineTasks().*/
    public abstract void initStart();

    /**Is run repeatedly throughout init until you press start.*/
    public abstract void initLoop();

    /**Is run once immediately after you press start.*/
    public abstract void mainStart();

    /**Is run once per loop after start, right after the task tree has been run. Most robot behavior belongs in tasks; this is useful for extra telemetry.*/
    public abstract void mainLoop();

    /**Is run once at the end of the opMode, whether the task tree finished or the opMode was stopped.*/
    public abstract void end();

    /**
     * Whether to build the OptimalityEngine at INIT. Override this to return false in opModes that
     * never drive to targets, such as a simple teleop or Calibration, so that they run without the
     * tables on the SD card. Drive and PIDHold throw at construction if this returns false.
     * @return true (the default) to load the SD card tables
     */
    public boolean buildOptimalityEngine() { return true; } // usually protected

}
