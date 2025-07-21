package net.agiledeveloper.nodup.image.processors;

import net.agiledeveloper.nodup.image.Image;
import net.agiledeveloper.nodup.image.processors.ImageProcessor.Collision;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static net.agiledeveloper.stubs.StubImage.ImageBuilder.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class ImageProcessorTest {

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

        public CollisionAssertion toBe(Image original, Image... duplicates) {
            Optional<Collision> optionalCollision = collisions.stream().findFirst();
            var collision = optionalCollision.orElseThrow();
            assertThat(collision.contains(original)).isTrue();
            assertThat(collision.contains(duplicates)).isTrue();
            return this;
        }

    }

}

