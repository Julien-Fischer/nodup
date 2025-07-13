package net.agiledeveloper;

import net.agiledeveloper.image.IOImage;
import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.processors.BruteForceProcessor;
import net.agiledeveloper.image.processors.ExifProcessor;
import net.agiledeveloper.image.processors.ImageProcessor;
import net.agiledeveloper.image.processors.ImageProcessor.Collision;
import net.agiledeveloper.image.processors.collision.CollisionDetector;
import net.agiledeveloper.image.processors.collision.HashCollisionDetector;
import net.agiledeveloper.image.processors.collision.PixelCollisionDetector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.time.format.DateTimeFormatter.ofPattern;

public class App {

    public static final Level LOG_LEVEL = Level.INFO;
    private static final Collider COLLIDER = Collider.PIXEL;
    private static final Processor PROCESSOR = Processor.EXIF;
    private static final Action ACTION = Action.COPY;
    public static final String COLLISION_BIN_NAME = "collision_bin";

    private static final Logger logger = Logger.getLogger(App.class.getSimpleName());


    public static void main(String[] args) {
        requireValidArguments(args);
        setLogLevel(LOG_LEVEL);

        ImageProcessor imageProcessor = PROCESSOR.algorithm;
        logger.info(() -> "Image processor: %s".formatted(PROCESSOR));
        logger.info(() -> "Collision algorithm: %s".formatted(COLLIDER));

        long start = System.nanoTime();

        String directory = args[0];
        Image[] images = at(directory);
        Collection<Collision> collisions = imageProcessor.detectCollisions(images);
        logger.info(() -> "#".repeat(80));
        logger.info(() -> "Found %s collisions:".formatted(collisions.size()));
        for (Collision collision : collisions) {
            logger.info(() -> "" + collision);
        }

        if (ACTION == Action.MOVE) {
            try {
                logger.info(() -> "#".repeat(80));
                logger.info(() -> "Moving %s duplicates to %s:".formatted(collisions.size(), COLLISION_BIN_NAME));
                move(collisions);
            } catch (IOException exception) {
                logger.severe("Could not move duplicates. Cause: " + exception.getMessage());
            }
        }

        long end = System.nanoTime();
        var duration = Duration.ofNanos(end - start);
        logger.info(() -> "Elapsed time: %s ms".formatted(duration.toMillis()));
    }

    private static void move(Collection<Collision> collisions) throws IOException {
        File bin = createBin();
        for (var collision : collisions) {
            var firstPath = collision.a().path();
            Path sourcePath = firstPath;
            Path targetPath = bin.toPath().resolve(sourcePath.getFileName());
            action(sourcePath, targetPath);
            logger.fine("File moved to: " + targetPath);
        }
    }

    private static void action(Path sourcePath, Path targetPath) throws IOException {
        switch (ACTION) {
            case MOVE -> Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            case COPY -> Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            default -> throw new IllegalArgumentException("Unsupported action: " + ACTION);
        }
    }

    private static File createBin() throws IOException {
        String userHome = System.getProperty("user.home");
        File newDir = new File(userHome, COLLISION_BIN_NAME);
        if (!newDir.exists() && !newDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + newDir.getAbsolutePath());
        }
        return newDir;
    }


    private static void requireValidArguments(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Missing required arguments");
        }
    }

    private static Image[] at(String directory) {
        Path dir = Paths.get(directory);
        try {
            return getFilesOnly(dir).stream()
                    .map(IOImage::new)
                    .toArray(Image[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Path> getFilesOnly(Path directory) throws IOException {
        try (var stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .toList();
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
