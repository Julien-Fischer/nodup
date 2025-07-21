package net.agiledeveloper.nodup.image.processors;

import net.agiledeveloper.nodup.image.Image;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static java.util.Arrays.asList;

public interface ImageProcessor {

    default Collection<Collision> detectCollisions(Image... images) {
        return detectCollisions(asList(images));
    }

    Collection<Collision> detectCollisions(Collection<Image> images);


    class Collision {

        private final Image original;
        private final Collection<Image> duplicates;

        public Collision(Image original, Image... duplicates) {
            this.original = original;
            this.duplicates = new ArrayList<>(asList(duplicates));
        }

        public Image original() {
            return original;
        }

        public Collection<Image> duplicates() {
            return new ArrayList<>(duplicates);
        }

        public boolean contains(Image... images) {
            for (var image : images) {
                if (original.equals(image) || duplicates.contains(image)) {
                    return true;
                }
            }
            return false;
        }

        void add(Image duplicate) {
            duplicates.add(duplicate);
        }

        @Override
        public String toString() {
            return "Collision %s vs %s".formatted(original, duplicates);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Collision collision)) return false;
            return Objects.equals(original, collision.original) && Objects.equals(duplicates, collision.duplicates);
        }

        @Override
        public int hashCode() {
            return Objects.hash(original, duplicates);
        }
    }

}
