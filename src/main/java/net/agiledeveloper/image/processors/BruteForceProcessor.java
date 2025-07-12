package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class BruteForceProcessor implements ImageProcessor {

    @Override
    public Collection<Collision> detectCollisions(Image... images) {
        Set<Image> read = new HashSet<>();
        List<Collision> results = new ArrayList<>();
        int i = 0;
        for (var image : images) {
            System.out.println("-".repeat(40));
            System.out.println("EXIF: " + i + "/" + images.length);
            System.out.println("-".repeat(40));
            System.out.println(image);
            if (read.contains(image)) continue;
            for (var other : images) {
                System.out.println("    " + other);
                if (!image.hasSize(other)) continue;
                if (read.contains(other) || image == other) continue;
                var potentialCollision = detectCollision(image, other);
                if (potentialCollision.isPresent()) {
                    read.add(other);
                    results.add(potentialCollision.get());
                }
            }
            i++;
        }
        return results;
    }

    @Override
    public Optional<Collision> detectCollision(Image imageA, Image imageB) {
        Hash a = hashPixels(imageA);
        Hash b = hashPixels(imageB);
        if (!a.equals(b)) {
            return Optional.empty();
        }
        return Optional.of(new Collision(a, imageA, imageB));
    }

    public static Hash hashPixels(Image image) {
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

}
