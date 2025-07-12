package net.agiledeveloper.image.processors;

import net.agiledeveloper.App;
import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.collision.CollisionDetector;

import java.util.*;
import java.util.logging.Logger;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;

public class BruteForceProcessor implements ImageProcessor {

    protected final Logger logger = Logger.getLogger(getClass().getSimpleName());


    private final CollisionDetector collisionDetector;


    public BruteForceProcessor(CollisionDetector collisionDetector) {
        this.collisionDetector = collisionDetector;
        logger.setLevel(App.LOG_LEVEL);
    }


    @Override
    public Collection<Collision> detectCollisions(Collection<Image> images) {
        Set<Image> read = new HashSet<>();
        List<Collision> results = new ArrayList<>();
        int i = 0;
        int size = images.size();
        for (var image : images) {
            logger.log(FINER, () -> "-".repeat(40));
            logger.log(FINER, printProgress(i, size));
            logger.log(FINER, () -> "-".repeat(40));
            logger.log(FINER, image::toString);
            if (read.contains(image)) continue;
            for (var other : images) {
                logger.log(FINEST, () -> "    " + other);
                if (!image.hasSize(other)) continue;
                if (read.contains(other) || image == other) continue;
                Optional<Collision> potentialCollision = collisionDetector.of(image, other);
                if (potentialCollision.isPresent()) {
                    read.add(other);
                    results.add(potentialCollision.get());
                }
            }
            i++;
        }
        return results;
    }

    private static String printProgress(int i, int n) {
        int percent = i / n * 100;
        return "Image: %s / %s (%s)".formatted(i, n, percent);
    }

}
