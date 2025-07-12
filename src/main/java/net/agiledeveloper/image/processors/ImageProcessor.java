package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;

import java.util.Collection;

import static java.util.Arrays.asList;

public interface ImageProcessor {

    default Collection<Collision> detectCollisions(Image... images) {
        return detectCollisions(asList(images));
    }

    Collection<Collision> detectCollisions(Collection<Image> images);

    record Collision(Image a, Image b) {

        @Override
        public String toString() {
            return "Collision %s vs %s".formatted(a, b);
        }
    }

}
