package net.agiledeveloper;

import java.util.Optional;

public class ImageProcessor {

    private ImageProcessor() {}

    public static Optional<HashCollision> detectCollision(Image a, Image b) {
        if (!a.hasSize(b)) {
            return Optional.empty();
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }


    public record HashCollision(String hash, Image a, Image b) {

    }

    public record Image(int width, int height) {

        public boolean hasSize(Image other) {
            return (
                    width == other.width &&
                    height == other.height
            );
        }

    }

}
