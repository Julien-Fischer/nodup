package net.agiledeveloper.image;

import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface Image {

    Path path();

    String format();

    long weight();

    int width();

    int height();

    default Dimension dimension() {
        return new Dimension(width(), height());
    }

    int[] pixels() throws IOException;

    default boolean hasSize(Image other) {
        return other.dimension().equals(dimension());
    }

    default String hash() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            int[] pixels = pixels();

            for (int pixel : pixels) {
                md.update((byte) (pixel >>> 24));
                md.update((byte) (pixel >>> 16));
                md.update((byte) (pixel >>> 8));
                md.update((byte) pixel);
            }

            return toHexadecimal(md.digest()).toString();
        } catch (NoSuchAlgorithmException | IOException cause) {
            throw new ReadException(cause);
        }
    }

    private static StringBuilder toHexadecimal(byte[] hashBytes) {
        var hex = new StringBuilder();
        for (byte b : hashBytes) {
            hex.append(String.format("%02x", b));
        }
        return hex;
    }

    record Dimension(int width, int height) {

        @Override
        public String toString() {
            return "%sx%spx".formatted(width, height);
        }
    }


    class ReadException extends RuntimeException {

        public ReadException(String message) {
            super(message);
        }

        public ReadException(Exception cause) {
            super(cause);
        }

    }

}
