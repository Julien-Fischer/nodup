package net.agiledeveloper;

import net.agiledeveloper.image.collision.CollisionDetector;
import net.agiledeveloper.image.collision.HashCollisionDetector;
import net.agiledeveloper.image.processors.ExifImageProcessor;
import net.agiledeveloper.image.processors.ImageProcessor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static net.agiledeveloper.ImageProcessorTest.ImageBuilder.aDogImage;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ExifProcessorTest extends ImageProcessorTest {

    private static final CollisionDetector collisionDetector = new HashCollisionDetector();

    public ExifProcessorTest() {
        super(new ExifImageProcessor(collisionDetector));
    }


    @Disabled("Implement format detection in exif processor")
    @Test
    void different_formats_do_not_collide() {
        var png = aDogImage().formatted("png").build();
        var jpg = aDogImage().formatted("jpg").build();

        Collection<ImageProcessor.Collision> collisions = processor.detectCollisions(png, jpg);

        assertThat(collisions).isEmpty();
    }

}

