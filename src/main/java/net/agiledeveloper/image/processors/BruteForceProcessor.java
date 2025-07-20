package net.agiledeveloper.image.processors;

import net.agiledeveloper.App;
import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.processors.collision.CollisionDetector;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
        var filesProcessed = new AtomicInteger(0);
        int size = images.size();
        var map = new HashMap<String, Collision>();

        for (var image : images) {
            var hash = image.hash();
            logger.finest(() -> "-".repeat(40));
            logger.finest(() -> printProgress(filesProcessed.incrementAndGet(), size));
            logger.finest(() -> "-".repeat(40));
            logger.finest(() -> "    " + hash + " | " + image);
            if (map.containsKey(hash)) {
                map.get(hash).add(image);
            } else {
                map.put(hash, new Collision(image));
            }
        }

        return map.values().stream()
                .filter(collision -> !collision.duplicates().isEmpty())
                .toList();
    }

    protected static String printProgress(int i, int n) {
        return printProgress(i, n, "Image");
    }

    protected static String printProgress(int i, int n, String prefix) {
        var percent = ((float) i / (float) n) * 100;
        return "%s: %s / %s (%s)".formatted(prefix, i, n, percent + "%");
    }

}
