package net.agiledeveloper.image.processors;

import net.agiledeveloper.App;
import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.processors.collision.CollisionDetector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

public class BruteForceProcessor implements ImageProcessor {

    protected final Logger logger = Logger.getLogger(getClass().getSimpleName());

    private final CollisionDetector collisionDetector;


    public BruteForceProcessor(CollisionDetector collisionDetector) {
        this.collisionDetector = collisionDetector;
        logger.setLevel(App.LOG_LEVEL);
    }


    private static String hash(Image image) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            int[] pixels = image.pixels();

            var buffer = ByteBuffer.allocate(4 * pixels.length);
            for (int pixel : pixels) {
                buffer.putInt(pixel);
            }
            byte[] imageData = buffer.array();

            md.update(imageData);
            byte[] hashBytes = md.digest();

            var hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
//            return new String(hashBytes);

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public Collection<Collision> detectCollisions(Collection<Image> images) {
        int i = 0;
        int size = images.size();
        var map = new HashMap<String, Collision>();

        for (var image : images) {
            var hash = hash(image);
            logger.finest(() -> "-".repeat(40));
            logger.finest(printProgress(i, size));
            logger.finest(() -> "-".repeat(40));
            logger.finest(() -> "    " + hash + " | " + image);
            if (map.containsKey(hash)) {
                map.get(hash).add(image);
            } else {
                map.put(hash, new Collision(image));
            }
            i++;
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
