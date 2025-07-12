package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;

import java.util.Collection;
import java.util.Optional;

import static java.util.Arrays.asList;

public interface ImageProcessor {

    default Collection<Collision> detectCollisions(Image... images) {
        return detectCollisions(asList(images));
    }

    Collection<Collision> detectCollisions(Collection<Image> images);

    Optional<Collision> detectCollision(Image imageA, Image imageB);

    record Collision(BruteForceProcessor.Hash hash, Image a, Image b) {

        @Override
        public String toString() {
            return "Collision %s vs %s".formatted(a, b);
        }
    }

}
