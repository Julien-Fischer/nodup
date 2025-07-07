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

