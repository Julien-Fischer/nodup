package net.agiledeveloper;

import net.agiledeveloper.image.ImageDeduplicator;
import net.agiledeveloper.image.ImageProvider;
import net.agiledeveloper.image.SimpleImageProvider;
import net.agiledeveloper.image.bin.Bin;
import net.agiledeveloper.image.bin.DateBin;
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
    private static final Collider COLLIDER = Collider.PIXEL;
    private static final Processor PROCESSOR = Processor.EXIF;
    public static final String COLLISION_BIN_NAME = "collision_bin";

    private static final Logger logger = Logger.getLogger(App.class.getSimpleName());
    public static ImageProvider imageProvider = new SimpleImageProvider();
    public static Bin bin = new DateBin();
    private static Action action = DEFAULT_ACTION;
    public static Level LOG_LEVEL = DEFAULT_LOG_LEVEL;


    public static void main(String[] args) {
        requireValidArguments(args);
        parseArguments(args);
        setLogLevel(LOG_LEVEL);

        var directory = args[0];
        logConfig(directory);

        var imageDeduplicator = new ImageDeduplicator(PROCESSOR.algorithm, imageProvider, bin);
        imageDeduplicator.execute(action, directory);
    }

    private static void logConfig(String directory) {
        logger.info(() -> "%s duplicates in %s".formatted(action, directory));
        logger.info(() -> "Image processor: %s".formatted(PROCESSOR));
        logger.info(() -> "Collision algorithm: %s".formatted(COLLIDER));
        logger.info(() -> "Log level: %s".formatted(LOG_LEVEL));
        logger.info(() -> "Bin: %s".formatted(bin.path()));
    }

    private static void parseArguments(String[] arguments) {
        Level logLevel = null;
        for (String argument : arguments) {
            action = readAction(argument);

            if (argument.startsWith("--log=")) {
                String levelString = argument.substring("--log=".length());
                if (logLevel == null) {
                    try {
                        logLevel = readLogLevel(levelString);
                        setLogLevel(logLevel);
                    } catch (IllegalArgumentException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
        }
    }

    public static Level readLogLevel(String levelName) {
        if (levelName == null) {
            throw new IllegalArgumentException("Level name cannot be null");
        }
        try {
            return Level.parse(levelName.trim().toUpperCase());
        } catch (IllegalArgumentException cause) {
            throw new IllegalArgumentException("Unknown logging level: " + levelName, cause);
        }
    }

    private static Action readAction(String argument) {
        return switch (argument) {
            case "-c", "--copy" -> Action.COPY;
            case "-m", "--move" -> Action.MOVE;
            case "-s", "--scan" -> Action.SCAN;
            default -> DEFAULT_ACTION;
        };
    }

    private static void requireValidArguments(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Missing required arguments");
        }
    }

    private static void setLogLevel(Level level) {
        LOG_LEVEL = level;
        logger.setLevel(level);
        for (var handler : Logger.getLogger("").getHandlers()) {
            handler.setFormatter(new MessageFormatter());
            handler.setLevel(level);
        }
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
