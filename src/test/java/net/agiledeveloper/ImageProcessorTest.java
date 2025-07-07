package net.agiledeveloper;

import net.agiledeveloper.ImageProcessor.HashCollision;
import net.agiledeveloper.ImageProcessor.Image;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ImageProcessorTest {

    @Test
    void different_dimensions() {
        var bigImage = new Image(64, 64);
        var smallImage = new Image(64, 128);

        Optional<HashCollision> potentialMatch = ImageProcessor.detectCollision(bigImage, smallImage);

        assertThat(potentialMatch).isEmpty();
    }

}

