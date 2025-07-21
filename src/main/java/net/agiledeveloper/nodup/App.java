package net.agiledeveloper.nodup;

import net.agiledeveloper.nodup.bin.Bin;
import net.agiledeveloper.nodup.bin.DatePathProvider;
import net.agiledeveloper.nodup.image.ImageDeduplicator;
import net.agiledeveloper.nodup.image.SimpleImageProvider;
import net.agiledeveloper.nodup.image.processors.ExifProcessor;
import net.agiledeveloper.nodup.ui.GUIDirectoryOpener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.time.format.DateTimeFormatter.ofPattern;

public class App {

    public static final Action DEFAULT_ACTION = Action.SCAN;
    public static final Level DEFAULT_LOG_LEVEL = Level.INFO;
    public static final String ROOT_DIR = "nodup";

    public static final Logger logger = Logger.getLogger(App.class.getSimpleName());
    public static Level LOG_LEVEL = DEFAULT_LOG_LEVEL;


    public static void main(String[] args) {
        var bin = new Bin(new DatePathProvider());
        var imageDeduplicator = new ImageDeduplicator(new ExifProcessor(), new SimpleImageProvider(), bin);
        var orchestrator = new Orchestrator(imageDeduplicator, new GUIDirectoryOpener());

        try {
            orchestrator.execute(args);
        } catch (IllegalArgumentException exception) {
            failAndExit(exception);
        }
    }

    @SuppressWarnings("java:S106")
    private static void failAndExit(Exception exception) {
        System.err.println("E: " + exception.getMessage());
        System.out.println();
        Orchestrator.printHelp();
        System.exit(1);
    }

    public static class MessageFormatter extends Formatter {

        private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord logRecord) {
            return String.format(
                    "[%s] %-7s - %s%n",
                    formatTimestamp(logRecord),
                    logRecord.getLevel().getName(),
                    formatMessage(logRecord)
            );
        }

        private String formatTimestamp(LogRecord logRecord) {
            return getLocalDateTime(logRecord).format(DATE_TIME_FORMATTER);
        }

        private static LocalDateTime getLocalDateTime(LogRecord logRecord) {
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(logRecord.getMillis()),
                    ZoneId.systemDefault()
            );
        }

    }


    public enum Action {

        SCAN,
        COPY,
        MOVE

    }

}
