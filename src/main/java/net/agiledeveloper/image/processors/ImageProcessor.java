package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;

import java.util.Collection;
import java.util.Optional;

public interface ImageProcessor {

    Collection<Collision> detectCollisions(Image... images);

    Optional<Collision> detectCollision(Image imageA, Image imageB);

    record Collision(BruteForceProcessor.Hash hash, Image a, Image b) {

        @Override
        public String toString() {
            return "Collision %s vs %s".formatted(a, b);
        }
    }

}
