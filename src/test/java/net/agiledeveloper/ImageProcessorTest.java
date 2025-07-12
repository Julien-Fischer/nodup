package net.agiledeveloper;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.processors.ImageProcessor;
import net.agiledeveloper.image.processors.ImageProcessor.Collision;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static net.agiledeveloper.ImageProcessorTest.ImageBuilder.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

abstract class ImageProcessorTest {

    protected final ImageProcessor processor;


    public ImageProcessorTest(ImageProcessor processor) {
        this.processor = processor;
    }


    @Test
    void different_dimensions() {
        Image big = aBigDog();
        Image small = aDog();

        Collection<Collision> potentialMatch = processor.detectCollisions(big, small);

        assertThat(potentialMatch).isEmpty();
    }

    @Test
    void different_images() {
        Image cat = aCat();
        Image dog = aDog();

        Collection<Collision> potentialMatch = processor.detectCollisions(cat, dog);

        assertThat(potentialMatch).isEmpty();
    }

    @Test
    void same_images() {
        Image a = aCat();
        Image b = aCat();

        Collection<Collision> potentialMatch = processor.detectCollisions(a, b);

        assertThat(potentialMatch).isNotEmpty();
    }

    @Test
    void detect_zero_collision() {
        Image[] images = {aDog(), aCat(), aBigDog()};

        Collection<Collision> collisions = processor.detectCollisions(images);

        assertThat(collisions).isEmpty();
    }

    @Test
    void detect_collisions() {
        var a = aDog();
        var b = aDog();
        Image[] images = {a, aBigDog(), b, aCat()};

        Collection<Collision> collisions = processor.detectCollisions(images);

        expect(collisions).toBe(a, b);
    }


    private static CollisionAssertion expect(Collection<Collision> collisions) {
        return new CollisionAssertion(collisions);
    }

    private record CollisionAssertion(Collection<Collision> collisions) {

        public CollisionAssertion toBe(Image a, Image b) {
            Optional<Collision> optionalCollision = collisions.stream().findFirst();
            var collision = optionalCollision.orElseThrow();
            assertThat(collision.a()).isEqualTo(a);
            assertThat(collision.b()).isEqualTo(b);
            return this;
        }

    }


    public static class ImageBuilder {

        private String name = "an image";
        private String format = "jpg";
        private Pixels pixels = Pixels.CAT;

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

        public Image build() {
            return new StubImage(name, pixels, format);
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

    private record StubImage(String name, Pixels pixelContent, String format) implements Image {

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
    }

    private enum Pixels {

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

}

