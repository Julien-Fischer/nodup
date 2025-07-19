package net.agiledeveloper.image.processors;

import net.agiledeveloper.App;
import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.processors.collision.CollisionDetector;

import java.util.*;
import java.util.logging.Logger;

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
            logger.finest(() -> "-".repeat(40));
            logger.finest(printProgress(i, size));
            logger.finest(() -> "-".repeat(40));
            logger.finest(image::toString);
            if (read.contains(image)) continue;
            for (var other : images) {
                logger.finest(() -> "    " + other);
                if (!image.hasSize(other) || read.contains(other) || image == other) continue;
                if (collisionDetector.collides(image, other)) {
                    read.add(other);
                    results.add(new Collision(image, other));
                }
            }
            i++;
        }
        return results;
    }

    protected static String printProgress(int i, int n) {
        return printProgress(i, n, "Image");
    }

    protected static String printProgress(int i, int n, String prefix) {
        var percent = ((float) i / (float) n) * 100;
        return "%s: %s / %s (%s)".formatted(prefix, i, n, percent + "%");
    }

}
