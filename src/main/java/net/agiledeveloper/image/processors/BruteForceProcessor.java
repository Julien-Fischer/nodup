package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.collision.CollisionDetector;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BruteForceProcessor implements ImageProcessor {

    protected static final Logger logger = Logger.getLogger(ImageProcessor.class.getSimpleName());

    private final CollisionDetector collisionDetector;


    public BruteForceProcessor(CollisionDetector collisionDetector) {
        this.collisionDetector = collisionDetector;
    }


    @Override
    public Collection<Collision> detectCollisions(Collection<Image> images) {
        Set<Image> read = new HashSet<>();
        List<Collision> results = new ArrayList<>();
        int i = 0;
        for (var image : images) {
            logger.log(Level.FINER, "-".repeat(40));
            logger.log(Level.FINER, "Image: " + i + "/" + images.size());
            logger.log(Level.FINER, "-".repeat(40));
            logger.log(Level.FINER, image.toString());
            if (read.contains(image)) continue;
            for (var other : images) {
                logger.log(Level.FINEST, "    " + other);
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

}
