package net.agiledeveloper;

import net.agiledeveloper.image.IOImage;
import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.collision.CollisionDetector;
import net.agiledeveloper.image.collision.HashCollisionDetector;
import net.agiledeveloper.image.collision.PixelCollisionDetector;
import net.agiledeveloper.image.processors.BruteForceProcessor;
import net.agiledeveloper.image.processors.ExifImageProcessor;
import net.agiledeveloper.image.processors.ImageProcessor;
import net.agiledeveloper.image.processors.ImageProcessor.Collision;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    public static final Level LOG_LEVEL = Level.FINEST;
    private static final Collider collider = Collider.PIXEL;
    private static final Processor processor = Processor.EXIF;

    private static final Logger logger = Logger.getLogger(App.class.getSimpleName());


    public static void main(String[] args) {
        requireValidArguments(args);
        logger.setLevel(LOG_LEVEL);


        ImageProcessor imageProcessor = processor.algorithm;
        logger.info(() -> "Image processor: %s".formatted(processor));
        logger.info(() -> "Collision algorithm: %s".formatted(collider));

        long start = System.nanoTime();

        String directory = args[0];
        Image[] images = at(directory);
        Collection<Collision> collisions = imageProcessor.detectCollisions(images);
        logger.info(() -> "#".repeat(40));
        logger.info(() -> "Found %s collisions:".formatted(collisions.size()));
        for (Collision collision : collisions) {
            logger.info(() -> "" + collision);
        }

        long end = System.nanoTime();
        logger.info(() -> "Elapsed time: %s ns".formatted(end - start));
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

    private enum Collider {

        HASH  (new HashCollisionDetector()),
        PIXEL (new PixelCollisionDetector());

        private final CollisionDetector algorithm;

        Collider(CollisionDetector algorithm) {
            this.algorithm = algorithm;
        }

    }

    private enum Processor {

        EXIF (new ExifImageProcessor(collider.algorithm)),
        BRUTE_FORCE (new BruteForceProcessor(collider.algorithm));

        private final ImageProcessor algorithm;

        Processor(ImageProcessor algorithm) {
            this.algorithm = algorithm;
        }

    }

}
