package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.collision.CollisionDetector;

import java.util.*;

public class BruteForceProcessor implements ImageProcessor {

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
            System.out.println("-".repeat(40));
            System.out.println("Image: " + i + "/" + images.size());
            System.out.println("-".repeat(40));
            System.out.println(image);
            if (read.contains(image)) continue;
            for (var other : images) {
                System.out.println("    " + other);
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
