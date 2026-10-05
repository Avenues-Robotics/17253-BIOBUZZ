package org.firstinspires.ftc.teamcode.peregrine.core.tasks;

import android.content.Context;
import android.os.Environment;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.PeregrineOpMode;
import org.firstinspires.ftc.teamcode.peregrine.core.utilities.Task;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.function.DoubleSupplier;

/**
 * <h3>Logs any values you choose to a timestamped CSV on the SD card, once per loop.</h3>
 *
 * <p>Each Logger writes a new file,
 * {@code /Android/data/com.qualcomm.ftcrobotcontroller/files/logs/log_yyyyMMdd_HHmmss.csv} on the card.
 * The first column is always {@code timestamp} (ms since the Logger was built). Add more columns with
 * {@link #addLogItem(String, DoubleSupplier)} before the Logger first runs, for example
 * {@code logger.addLogItem("lift", () -> hardware.lift.getCurrentPosition())}.</p>
 *
 * <p>{@link #addDrivetrainItems()} adds the columns the desktop drivetrain fit reads
 * (TABLE_FORMAT.MD §8.6), which is what Calibration uses.</p>
 *
 * <p>Never finishes on its own: run() always returns false. Run it in a ParallelTask or
 * ParallelRaceTask alongside the tasks you want to record.</p>
 */
public class Logger extends Task {

    // One CSV column: its header name and the function that reads its value each loop.
    static class LogItem {
        String name;
        DoubleSupplier evaluator;

        public LogItem(String name, DoubleSupplier evaluator) {
            this.name = name;
            this.evaluator = evaluator;
        }

        public double evaluate() {
            return evaluator.getAsDouble();
        }
    }

    ArrayList<LogItem> logItems;
    // Set on the first run(). After that, columns can no longer be added.
    boolean hasRun;

    PeregrineOpMode opMode;

    // Cleared when no card is found or the logs directory can't be created.
    boolean sdInserted = true;
    // NOTE: unlike OptimalityEngine.findSdCard(), this is the app-specific directory on the card
    // (/storage/XXXX-XXXX/Android/data/<package>/files), not the card root, so logs land in <that>/logs.
    File sdCard;
    FileWriter writer;
    File logFile;

    // Measures the timestamp column.
    ElapsedTime time;

    public Logger(PeregrineOpMode opMode, String filename) {
        this.opMode = opMode;
        logItems = new ArrayList<>();
        hasRun = false;

        sdCard = findSdCard();
        // No removable storage found.
        if(!sdInserted || sdCard == null) {
            throw new IllegalStateException("No removable storage found.");
        }

        File logDir = new File(sdCard, "logs");
        if(!logDir.exists()) {
            // The logs directory could not be created, usually because the card is not mounted.
            if(!logDir.mkdirs()) {
                sdInserted = false;
                if (!Environment.getExternalStorageState(sdCard).equals(Environment.MEDIA_MOUNTED)) {
                    throw new IllegalStateException("Card not mounted, ensure it's formatted to FAT32");
                }
                throw new IllegalStateException("Log directory couldn't be created");
            }
        }

        // One new file per construction, e.g. log_20260910_153000.csv.
        logFile = new File(logDir, filename + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".csv");

        try {
            writer = new FileWriter(logFile, true);
            writer.write("timestamp");
            writer.flush();
        } catch (Exception e) {
            try{ writer.close(); } catch (Exception ignored) {}
            throw new RuntimeException("unexpected error while writing log", e);
        }

        time = new ElapsedTime();
    }

    /**
     * Finds the SD card and creates a new log file with just the {@code timestamp} header.
     *
     * @param opMode the running opMode
     * @throws IllegalStateException if there is no SD card, it isn't mounted, or the logs directory
     * can't be created.
     * @throws RuntimeException if the log file can't be written.
     */
    public Logger(PeregrineOpMode opMode) {
        this(opMode, "log");
    }

    /**
     * Adds a column to the log. Must be called before the Logger's first run().
     *
     * @param name the column header
     * @param value read once per loop to fill the column, usually a lambda such as
     *              {@code () -> hardware.lift.getCurrentPosition()}
     * @throws IllegalStateException if the Logger has already run.
     */
    public void addLogItem(String name, DoubleSupplier value) {
        if(hasRun) throw new IllegalStateException("New log items can only be added before logger is run");
        logItems.add(new LogItem(name, value));
        try {
            writer.write("," + name);
            writer.flush();
        } catch (Exception e) {
            try{ writer.close(); } catch (Exception ignored) {}
            throw new RuntimeException("unexpected error while writing log", e);
        }
    }

    /** Appends one CSV row with the timestamp and the current value of every column. It flushes every row, so a crash loses at most one line. */
    @Override
    public boolean run() {
        hasRun = true;
        try {
            writer.write("\n" + time.milliseconds());
            for (LogItem logItem : logItems) {
                writer.write("," + logItem.evaluate());
            }
            writer.flush();
        } catch (Exception e) {
            try{ writer.close(); } catch (Exception ignored) {}
            throw new RuntimeException("unexpected error while writing log", e);
        }
        return false;
    }

    // NOTE: the FileWriter is never closed. Each row is flushed, so no data is lost.
    @Override
    public void end() {}

    // Starts a brand-new log file with the same columns.
    @Override
    public Task reset() {
        Logger output = new Logger(opMode);
        for (LogItem logItem : logItems) {
            output.addLogItem(logItem.name, logItem.evaluator);
        }
        return output;
    }

    /**
     * Adds the columns the desktop drivetrain fitter expects: {@code FR, FL, BR, BL} (motor powers),
     * {@code x, y} (cm), {@code h} (rad), {@code x_vel, y_vel} (cm/s) and {@code h_vel} (rad/s). Pose
     * and velocities are field frame, as reported by the Pinpoint.
     */
    public void addDrivetrainItems() {
        addLogItem("FR", () -> opMode.hardware.FR.getPower());
        addLogItem("FL", () -> opMode.hardware.FL.getPower());
        addLogItem("BR", () -> opMode.hardware.BR.getPower());
        addLogItem("BL", () -> opMode.hardware.BL.getPower());
        addLogItem("x",  () -> opMode.localizer.getPose().getX(DistanceUnit.CM));
        addLogItem("y",  () -> opMode.localizer.getPose().getY(DistanceUnit.CM));
        addLogItem("h",  () -> opMode.localizer.getPose().getHeading(AngleUnit.RADIANS));
        addLogItem("x_vel", () -> opMode.localizer.getVelX(DistanceUnit.CM));
        addLogItem("y_vel", () -> opMode.localizer.getVelY(DistanceUnit.CM));
        addLogItem("h_vel", () -> opMode.localizer.getHeadingVelocity(UnnormalizedAngleUnit.RADIANS));
    }

    // Returns the app-specific directory on the first removable volume, or null (and clears sdInserted).
    File findSdCard() {
        Context context = AppUtil.getInstance().getActivity();
        try {
            if(context == null) throw new Exception();
            File[] externalDirs = context.getExternalFilesDirs(null);

            for(File dir : externalDirs) {
                if (dir == null) continue;
                if (Environment.isExternalStorageRemovable(dir)) return dir;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while finding SD Card", e);
        }
        sdInserted = false;
        return null;
    }
}
