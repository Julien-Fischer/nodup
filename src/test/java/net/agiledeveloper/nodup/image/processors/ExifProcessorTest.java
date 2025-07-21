package net.agiledeveloper.nodup.image.processors;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static net.agiledeveloper.stubs.StubImage.ImageBuilder.aBigDogImage;
import static net.agiledeveloper.stubs.StubImage.ImageBuilder.aDogImage;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ExifProcessorTest extends ImageProcessorTest {

    public ExifProcessorTest() {
        super(new ExifProcessor());
    }


    @Test
    void different_formats_do_not_collide() {
        var png = aDogImage().formatted("png").build();
        var jpg = aDogImage().formatted("jpg").build();

        Collection<ImageProcessor.Collision> collisions = processor.detectCollisions(png, jpg);

        assertThat(collisions).isEmpty();
    }

    @Test
    void same_formats_but_different_dimension_do_not_collide() {
        var png = aBigDogImage().formatted("png").build();
        var jpg = aDogImage().formatted("png").build();

        Collection<ImageProcessor.Collision> collisions = processor.detectCollisions(png, jpg);

        assertThat(collisions).isEmpty();
    }

    @Test
    void different_weights_do_not_collide() {
        var light = aDogImage().weighting(2).build();
        var lighter = aDogImage().weighting(1).build();

        Collection<ImageProcessor.Collision> collisions = processor.detectCollisions(lighter, light);

        assertThat(collisions).isEmpty();
    }
}

