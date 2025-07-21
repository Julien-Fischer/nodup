package net.agiledeveloper.nodup.image.processors;

import net.agiledeveloper.nodup.image.Image;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static net.agiledeveloper.nodup.App.logger;

public class BucketProcessor implements ImageProcessor {

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
