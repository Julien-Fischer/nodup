package net.agiledeveloper;

import net.agiledeveloper.ImageProcessor.Collision;
import net.agiledeveloper.ImageProcessor.Image;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ImageProcessorTest {

    @Test
    void different_dimensions() {
        Image big = aBigDog();
        Image small = aDog();

        Optional<Collision> potentialMatch = ImageProcessor.detectCollision(big, small);

        assertThat(potentialMatch).isEmpty();
    }

    @Test
    void different_images() {
        Image cat = aCat();
        Image dog = aDog();

        Optional<Collision> potentialMatch = ImageProcessor.detectCollision(cat, dog);

        assertThat(potentialMatch).isEmpty();
    }

    @Test
    void same_images() {
        Image a = aCat();
        Image b = aCat();

        Optional<Collision> potentialMatch = ImageProcessor.detectCollision(a, b);

        assertThat(potentialMatch).isPresent();
    }


    private static Image aCat() {
        return new StubImage(new int[] {0, 0, 0, 0});
    }

    private static Image aDog() {
        return new StubImage(new int[] {1, 1, 1, 1});
    }

    private static Image aBigDog() {
        return new StubImage(new int[] {1, 1, 1, 1, 1, 1, 1, 1});
    }

    private record StubImage(int[] pixels) implements Image {

        @Override
        public int width() {
            return pixels.length / 2;
        }

        @Override
        public int height() {
            return pixels.length / 2;
        }

    }

}

