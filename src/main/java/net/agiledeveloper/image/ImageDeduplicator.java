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


    public void execute(Action action, Path directory) {
        this.action = action;

        long start = System.nanoTime();

        logSeparator();
        Image[] images = imageProvider.imagesAt(directory);
        logImages(directory, images);

        Collection<Collision> collisions = imageProcessor.detectCollisions(images);

        logSeparator();
        logCollisions(collisions);

        if (action != SCAN) {
            processDuplicates(action, collisions);
        }

        logDurationSince(start);
    }

    private void processDuplicates(Action action, Collection<Collision> collisions) {
        try {
            logSeparator();
            logger.info(() -> "About to [%s] %s duplicates to %s:".formatted(action, collisions.size(), bin.path()));
            for (var collision : collisions) {
                Path sourcePath = collision.a().path();
                Path targetPath = bin.path().resolve(sourcePath.getFileName());
                performAction(sourcePath, targetPath);
                logger.fine(() -> "File moved to: " + targetPath);
            }
            logger.info(() -> "Done [%s] %s duplicates to %s:".formatted(action, collisions.size(), bin.path()));
        } catch (IOException exception) {
            logger.severe("Could not %s duplicates. Cause: %s".formatted(action, exception.getMessage()));
        }
    }

    private void performAction(Path sourcePath, Path targetPath) throws IOException {
        switch (action) {
            case MOVE -> Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            case COPY -> Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            default -> throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }

    public Path binRoot() {
        return bin.root();
    }

    private void logDurationSince(long start) {
        long end = System.nanoTime();
        var duration = Duration.ofNanos(end - start);
        logger.info(() -> "Elapsed time: %s ms".formatted(duration.toMillis()));
    }

    private void logSeparator() {
        logger.info(() -> "#".repeat(80));
    }

    private void logImages(Path directory, Image[] images) {
        logger.info(() -> "Found %s images in %s".formatted(images.length, directory));
    }

    private void logCollisions(Collection<Collision> collisions) {
        logger.info(() -> "Found %s collisions:".formatted(collisions.size()));
        collisions.forEach(collision -> logger.info(collision.toString()));
    }

}
