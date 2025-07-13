package net.agiledeveloper.stubs;

import net.agiledeveloper.image.Image;

import java.nio.file.Path;
import java.nio.file.Paths;

public record StubImage(String name, Pixels pixelContent, String format, long weight) implements Image {

    @Override
    public Path path() {
        return Paths.get("/fake/path/for/stubbing");
    }

    @Override
    public int[] pixels() {
        return pixelContent.pixels;
    }

    @Override
    public int width() {
        return pixelContent.length() / 2;
    }

    @Override
    public int height() {
        return pixelContent.length() / 2;
    }

    @Override
    public String toString() {
        return name;
    }


    public enum Pixels {

        BIG_DOG (new int[] {1, 1, 1, 1, 1, 1, 1, 1}),
        DOG     (new int[] {1, 1, 1, 1}),
        CAT     (new int[] {0, 0, 0, 0});

        private final int[] pixels;

        Pixels(int[] pixels) {
            this.pixels = pixels;
        }

        int length() {
            return pixels.length;
        }

    }


    public static class ImageBuilder {

        private String name = "an image";
        private String format = "jpg";
        private Pixels pixels = Pixels.CAT;
        private Integer megaOctets = 10;

        public ImageBuilder named(String name) {
            this.name = name;
            return this;
        }

        public ImageBuilder formatted(String format) {
            this.format = format;
            return this;
        }

        public ImageBuilder withPixels(Pixels pixels) {
            this.pixels = pixels;
            return this;
        }

        public ImageBuilder weighting(int megaOctets) {
            this.megaOctets = megaOctets;
            return this;
        }

        public Image build() {
            return new StubImage(name, pixels, format, megaOctets);
        }


        public static Image aCat() {
            return aCatImage()
                    .formatted("jpg")
                    .build();
        }

        public static Image aDog() {
            return aDogImage()
                    .formatted("jpg")
                    .build();
        }

        public static Image aBigDog() {
            return aBigDogImage()
                    .formatted("jpg")
                    .build();
        }

        public static ImageBuilder aBigDogImage() {
            return new ImageBuilder()
                    .named("a_big_dog")
                    .withPixels(Pixels.BIG_DOG);
        }

        public static ImageBuilder aDogImage() {
            return new ImageBuilder()
                    .named("a_dog")
                    .withPixels(Pixels.DOG);
        }

        public static ImageBuilder aCatImage() {
            return new ImageBuilder()
                    .named("a_cat")
                    .withPixels(Pixels.CAT);
        }

    }

}
