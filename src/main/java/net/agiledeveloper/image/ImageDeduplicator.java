package net.agiledeveloper.image;

import net.agiledeveloper.App.Action;
import net.agiledeveloper.image.bin.Bin;
import net.agiledeveloper.image.bin.Bin.BinException;
import net.agiledeveloper.image.processors.ImageProcessor;
import net.agiledeveloper.image.processors.ImageProcessor.Collision;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.logging.Logger;

import static net.agiledeveloper.App.Action.SCAN;

public class ImageDeduplicator {

    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    private final ImageProcessor imageProcessor;
    private final ImageProvider imageProvider;
    private final Bin bin;


    public ImageDeduplicator(ImageProcessor imageProcessor, ImageProvider imageProvider, Bin bin) {
        this.imageProcessor = imageProcessor;
        this.imageProvider = imageProvider;
        this.bin = bin;
    }


    public void execute(Action action, Path directory) {
        long start = System.nanoTime();

        logSeparator();
        logger.info("Scanning directory...");
        Image[] images = imageProvider.imagesAt(directory);
        logImages(directory, images);

        logger.info("Checking for duplicates...");
        Collection<Collision> collisions = imageProcessor.detectCollisions(images);

        logSeparator();
        logCollisions(collisions);

        if (action != SCAN) {
            processDuplicates(action, collisions);
        }

        logDurationSince(start);
    }

    private void processDuplicates(Action action, Collection<Collision> collisions) {
        logSeparator();
        Collection<Path> duplicates = collisions.stream()
                .map(collision -> collision.a().path())
                .toList();
        try {
            bin.accept(action, duplicates);
        } catch (BinException exception) {
            logger.severe("Could not %s duplicates. Cause: %s".formatted(action, exception.getMessage()));
        }
    }

    public Bin bin() {
        return bin;
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
        logger.info(() -> "Found %s (potential) images in %s".formatted(images.length, directory));
    }

    private void logCollisions(Collection<Collision> collisions) {
        logger.info(() -> "Found %s collisions:".formatted(collisions.size()));
        collisions.forEach(collision -> logger.info(collision.toString()));
    }

}
