package net.agiledeveloper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;

public class ImageProcessor {

    private ImageProcessor() {
    }

    public static Collection<Collision> detectCollisions(Image... images) {
        return emptyList();
    }

    public static Optional<Collision> detectCollision(Image imageA, Image imageB) {
        if (!imageA.hasSize(imageB)) {
            return Optional.empty();
        }
        Hash a = hashPixels(imageA);
        Hash b = hashPixels(imageB);
        if (!a.equals(b)) {
            return Optional.empty();
        }
        return Optional.of(new Collision(a, imageA, imageB));
    }

    public static Hash hashPixels(Image image) {
        int[] pixels = image.pixels();
        var messageDigest = getMessageDigest();
        for (int pixel : pixels) {
            messageDigest.update((byte) (pixel >> 24));
            messageDigest.update((byte) (pixel >> 16));
            messageDigest.update((byte) (pixel >> 8));
            messageDigest.update((byte) pixel);
        }
        return new Hash(messageDigest.digest());
    }

    private static MessageDigest getMessageDigest() {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return sha256;
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

        int[] pixels();

        default boolean hasSize(Image other) {
            return (
                    width() == other.width() &&
                    height() == other.height()
            );
        }

    }

}
