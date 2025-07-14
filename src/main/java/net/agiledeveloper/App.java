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
import java.util.Optional;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.stream;

public class App {

    public static final Action DEFAULT_ACTION = Action.SCAN;
    public static final Level DEFAULT_LOG_LEVEL = Level.INFO;
    private static final Collider COLLIDER = Collider.PIXEL;
    private static final Processor PROCESSOR = Processor.EXIF;
    public static final String APP_DIR = "nodup";
    public static final String COLLISION_BIN_NAME = "%s/bin".formatted(APP_DIR);

    private static final Logger logger = Logger.getLogger(App.class.getSimpleName());
    public static ImageProvider imageProvider = new SimpleImageProvider();
    public static Bin bin = new DateBin();
    private static Action action = DEFAULT_ACTION;
    public static Level LOG_LEVEL = DEFAULT_LOG_LEVEL;


    public static void main(String[] args) {
        if (isHelpMessage(args)) {
            printHelp();
            return;
        }

        Optional<Exception> exception = parseArguments(args);
        if (exception.isPresent()) {
            showError(exception.get());
        } else {
            processCommand(args);
        }
    }

    @SuppressWarnings("java:S106")
    private static void showError(Exception exception) {
        System.err.println(exception.getMessage());
        printHelp();
    }

    private static void processCommand(String[] args) {
        setLogLevel(LOG_LEVEL);

        var directory = readDirectory(args);
        logConfig(directory);

        var imageDeduplicator = new ImageDeduplicator(PROCESSOR.algorithm, imageProvider, bin);
        imageDeduplicator.execute(action, directory);
    }

    private static boolean isHelpMessage(String[] args) {
        return stream(args)
                .anyMatch(argument -> argument.equals("-h") || argument.equals("--help"));
    }

    @SuppressWarnings("java:S106")
    private static void printHelp() {
        System.out.println("""
Usage:
  nodup [/path/to/dir] [OPTIONS]

Positional parameters:
  $1               (Optional) The path to the directory to process

Options:
  --log            Set the logging level (e.g., severe, warning, info, fine, finer, finest).

Flags:
  -c, --copy       Copy files in the directory.
  -m, --move       Move files in the directory.
  -s, --scan       Scan the directory and display file information.
  -h, --help       Print this help message and exit
""");
    }

    private static String readDirectory(String[] args) {
        boolean isDefined = args.length > 0 && !args[0].startsWith("-");
        return isDefined ? args[0] : System.getProperty("user.dir");
    }

    private static void logConfig(String directory) {
        logger.info(() -> "%s duplicates in %s".formatted(action, directory));
        logger.info(() -> "Image processor: %s".formatted(PROCESSOR));
        logger.info(() -> "Collision algorithm: %s".formatted(COLLIDER));
        logger.info(() -> "Log level: %s".formatted(LOG_LEVEL));
        logger.info(() -> "Bin: %s".formatted(bin.path()));
    }

    private static Optional<Exception> parseArguments(String[] arguments) {
        try {
            processLogLevel(arguments);

            for (String argument : arguments) {
                action = readAction(argument);
            }
            return Optional.empty();
        } catch (IllegalArgumentException exception) {
            return Optional.of(exception);
        }
    }

    private static void processLogLevel(String[] arguments) {
        stream(arguments)
                .filter(argument -> argument.startsWith("--log="))
                .findFirst()
                .map(App::parseLogLevel)
                .ifPresent(App::setLogLevel);
    }

    private static Level parseLogLevel(String argument) {
        String levelString = argument.substring("--log=".length());
        return readLogLevel(levelString);
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
