package net.agiledeveloper;

import net.agiledeveloper.image.ImageDeduplicator;
import net.agiledeveloper.image.SimpleImageProvider;
import net.agiledeveloper.image.bin.Bin;
import net.agiledeveloper.image.bin.DatePathProvider;
import net.agiledeveloper.image.processors.BruteForceProcessor;
import net.agiledeveloper.image.processors.ExifProcessor;
import net.agiledeveloper.image.processors.ImageProcessor;
import net.agiledeveloper.image.processors.collision.CollisionDetector;
import net.agiledeveloper.image.processors.collision.HashCollisionDetector;
import net.agiledeveloper.image.processors.collision.PixelCollisionDetector;

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
    public static final Collider COLLIDER = Collider.PIXEL;
    public static final Processor PROCESSOR = Processor.EXIF;
    public static final String ROOT_DIR = "nodup";

    public static final Logger logger = Logger.getLogger(App.class.getSimpleName());
    public static Level LOG_LEVEL = DEFAULT_LOG_LEVEL;


    public static void main(String[] args) {
        var bin = new Bin(new DatePathProvider());
        var imageDeduplicator = new ImageDeduplicator(PROCESSOR.algorithm, new SimpleImageProvider(), bin);
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
                var date = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(logRecord.getMillis()),
                        ZoneId.systemDefault()
                );
                return String.format("[%s] %-7s %s - %s%n",
                        date.format(DATE_TIME_FORMATTER),
                        logRecord.getLevel().getName(),
                        logRecord.getLoggerName(),
                        formatMessage(logRecord)
                );
            }

    }

    public enum Collider {

        HASH  (new HashCollisionDetector()),
        PIXEL (new PixelCollisionDetector());

        public final CollisionDetector algorithm;

        Collider(CollisionDetector algorithm) {
            this.algorithm = algorithm;
        }

    }

    public enum Processor {

        EXIF (new ExifProcessor(COLLIDER.algorithm)),
        BRUTE_FORCE (new BruteForceProcessor(COLLIDER.algorithm));

        public final ImageProcessor algorithm;

        Processor(ImageProcessor algorithm) {
            this.algorithm = algorithm;
        }

    }

    public enum Action {

        SCAN,
        COPY,
        MOVE

    }

}
