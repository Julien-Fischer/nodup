package net.agiledeveloper;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.ImageProvider;
import net.agiledeveloper.image.SimpleImageProvider;
import net.agiledeveloper.image.bin.Bin;
import net.agiledeveloper.image.bin.DateBin;
import net.agiledeveloper.image.processors.BruteForceProcessor;
import net.agiledeveloper.image.processors.ExifProcessor;
import net.agiledeveloper.image.processors.ImageProcessor;
import net.agiledeveloper.image.processors.ImageProcessor.Collision;
import net.agiledeveloper.image.processors.collision.CollisionDetector;
import net.agiledeveloper.image.processors.collision.HashCollisionDetector;
import net.agiledeveloper.image.processors.collision.PixelCollisionDetector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.time.format.DateTimeFormatter.ofPattern;

public class App {

    public static final Level LOG_LEVEL = Level.INFO;
    private static final Collider COLLIDER = Collider.PIXEL;
    private static final Processor PROCESSOR = Processor.EXIF;
    private static final Action DEFAULT_ACTION = Action.SCAN;
    public static final String COLLISION_BIN_NAME = "collision_bin";
    public static ImageProvider imageProvider = new SimpleImageProvider();
    public static Bin bin = new DateBin();

    private static final Logger logger = Logger.getLogger(App.class.getSimpleName());

    private static Action ACTION;


    public static void main(String[] args) {
        requireValidArguments(args);
        parseArguments(args);
        setLogLevel(LOG_LEVEL);

        ImageProcessor imageProcessor = PROCESSOR.algorithm;
        logger.info(() -> "Mode: %s".formatted(ACTION));
        logger.info(() -> "Image processor: %s".formatted(PROCESSOR));
        logger.info(() -> "Collision algorithm: %s".formatted(COLLIDER));

        long start = System.nanoTime();

        String directory = args[0];
        Image[] images = imageProvider.imagesAt(directory);
        logger.info("Found %s images in %s".formatted(images.length, directory));
        Collection<Collision> collisions = imageProcessor.detectCollisions(images);
        logger.info(() -> "#".repeat(80));
        logger.info(() -> "Found %s collisions:".formatted(collisions.size()));
        for (Collision collision : collisions) {
            logger.info(() -> "" + collision);
        }

        if (ACTION == Action.MOVE) {
            try {
                logger.info(() -> "#".repeat(80));
                logger.info(() -> "About to [%s] %s duplicates to %s:".formatted(ACTION, collisions.size(), bin.path()));
                processDuplicate(collisions);
                logger.info(() -> "Done [%s] %s duplicates to %s:".formatted(ACTION, collisions.size(), bin.path()));
            } catch (IOException exception) {
                logger.severe("Could not %s duplicates. Cause: %s".formatted(ACTION, exception.getMessage()));
            }
        }

        long end = System.nanoTime();
        var duration = Duration.ofNanos(end - start);
        logger.info(() -> "Elapsed time: %s ms".formatted(duration.toMillis()));
    }


    private static void parseArguments(String[] arguments) {
        for (String argument : arguments) {
            ACTION = readAction(argument);
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


    private static void processDuplicate(Collection<Collision> collisions) throws IOException {
        for (var collision : collisions) {
            Path sourcePath = collision.a().path();
            Path targetPath = bin.path().resolve(sourcePath.getFileName());
            System.out.println('a');
            performAction(sourcePath, targetPath);
            logger.fine("File moved to: " + targetPath);
        }
    }

    private static void performAction(Path sourcePath, Path targetPath) throws IOException {
        switch (ACTION) {
            case MOVE -> Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            case COPY -> Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            default -> throw new IllegalArgumentException("Unsupported action: " + ACTION);
        }
    }


    private static void requireValidArguments(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Missing required arguments");
        }
    }

    private static void setLogLevel(Level level) {
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

    private enum Collider {

        HASH  (new HashCollisionDetector()),
        PIXEL (new PixelCollisionDetector());

        private final CollisionDetector algorithm;

        Collider(CollisionDetector algorithm) {
            this.algorithm = algorithm;
        }

    }

    private enum Processor {

        EXIF (new ExifProcessor(COLLIDER.algorithm)),
        BRUTE_FORCE (new BruteForceProcessor(COLLIDER.algorithm));

        private final ImageProcessor algorithm;

        Processor(ImageProcessor algorithm) {
            this.algorithm = algorithm;
        }

    }

    private enum Action {

        SCAN,
        COPY,
        MOVE

    }

}
