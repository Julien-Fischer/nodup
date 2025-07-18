package net.agiledeveloper.image.processors.collision;

import net.agiledeveloper.image.Image;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public class HashCollisionDetector implements CollisionDetector {

    @Override
    public boolean collides(Image imageA, Image imageB) {
        Hash a = hashPixels(imageA);
        Hash b = hashPixels(imageB);
        return a.equals(b);
    }

    private static Hash hashPixels(Image image) {
        var pixels = readPixels(image);
        var messageDigest = getMessageDigest();
        for (int pixel : pixels) {
            messageDigest.update((byte) (pixel >> 24));
            messageDigest.update((byte) (pixel >> 16));
            messageDigest.update((byte) (pixel >> 8));
            messageDigest.update((byte) pixel);
        }
        return new Hash(messageDigest.digest());
    }

    private static int[] readPixels(Image image) {
        try {
            return image.pixels();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private record Hash(byte[] bytes) {
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

}
