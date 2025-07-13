package net.agiledeveloper.processors;

import net.agiledeveloper.image.processors.ExifProcessor;
import net.agiledeveloper.image.processors.ImageProcessor;
import net.agiledeveloper.image.processors.collision.CollisionDetector;
import net.agiledeveloper.image.processors.collision.HashCollisionDetector;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static net.agiledeveloper.processors.ImageProcessorTest.ImageBuilder.aBigDogImage;
import static net.agiledeveloper.processors.ImageProcessorTest.ImageBuilder.aDogImage;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ExifProcessorTest extends ImageProcessorTest {

    private static final CollisionDetector collisionDetector = new HashCollisionDetector();

    public ExifProcessorTest() {
        super(new ExifProcessor(collisionDetector));
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

