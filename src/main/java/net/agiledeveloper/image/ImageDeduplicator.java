package net.agiledeveloper.image;

import net.agiledeveloper.App.Action;
import net.agiledeveloper.image.bin.Bin;
import net.agiledeveloper.image.processors.ImageProcessor;
import net.agiledeveloper.image.processors.ImageProcessor.Collision;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Collection;
import java.util.logging.Logger;

import static net.agiledeveloper.App.Action.SCAN;

public class ImageDeduplicator {

    private final Logger logger = Logger.getLogger(ImageDeduplicator.class.getSimpleName());

    private final ImageProcessor imageProcessor;
    private final ImageProvider imageProvider;
    private final Bin bin;

    private Action action;


    public ImageDeduplicator(ImageProcessor imageProcessor, ImageProvider imageProvider, Bin bin) {
        this.imageProcessor = imageProcessor;
        this.imageProvider = imageProvider;
        this.bin = bin;
    }


    public void execute(Action action, String directory) {
        this.action = action;

        long start = System.nanoTime();

        Image[] images = imageProvider.imagesAt(directory);
        logger.info(() -> "Found %s images in %s".formatted(images.length, directory));
        Collection<Collision> collisions = imageProcessor.detectCollisions(images);
        logger.info(() -> "#".repeat(80));
        logger.info(() -> "Found %s collisions:".formatted(collisions.size()));
        for (Collision collision : collisions) {
            logger.info(() -> "" + collision);
        }

        if (action != SCAN) {
            try {
                logger.info(() -> "#".repeat(80));
                logger.info(() -> "About to [%s] %s duplicates to %s:".formatted(action, collisions.size(), bin.path()));
                processDuplicate(collisions);
                logger.info(() -> "Done [%s] %s duplicates to %s:".formatted(action, collisions.size(), bin.path()));
            } catch (IOException exception) {
                logger.severe("Could not %s duplicates. Cause: %s".formatted(action, exception.getMessage()));
            }
        }

        long end = System.nanoTime();
        var duration = Duration.ofNanos(end - start);
        logger.info(() -> "Elapsed time: %s ms".formatted(duration.toMillis()));
    }

    private void processDuplicate(Collection<Collision> collisions) throws IOException {
        for (var collision : collisions) {
            Path sourcePath = collision.a().path();
            Path targetPath = bin.path().resolve(sourcePath.getFileName());
            System.out.println('a');
            performAction(sourcePath, targetPath);
            logger.fine("File moved to: " + targetPath);
        }
    }

    private void performAction(Path sourcePath, Path targetPath) throws IOException {
        switch (action) {
            case MOVE -> Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            case COPY -> Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            default -> throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }

}
