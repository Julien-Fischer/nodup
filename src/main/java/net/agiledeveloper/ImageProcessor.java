package net.agiledeveloper;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class ImageProcessor {

    private ImageProcessor() {
    }

    public static Optional<Collision> detectCollision(Image imageA, Image imageB) {
        if (!imageA.hasSize(imageB)) {
            return Optional.empty();
        }
        throw new UnsupportedOperationException("not implemented yet");
    }

    public record Hash(byte[] bytes) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Hash(byte[] otherBytes))) return false;
            return Objects.deepEquals(bytes, otherBytes);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes);
        }

        @Override
        public String toString() {
            return new String(bytes);
        }
    }

    public record Collision(Hash hash, Image a, Image b) {

    }

    public interface Image {

        int width();

        int height();

        default boolean hasSize(Image other) {
            return (
                    width() == other.width() &&
                    height() == other.height()
            );
        }

    }

}
