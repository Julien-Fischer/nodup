package net.agiledeveloper;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.processors.BruteForceProcessor;
import net.agiledeveloper.image.processors.ImageProcessor.Collision;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class BruteForceProcessorTest {

    private final BruteForceProcessor processor = new BruteForceProcessor();

    @Test
    void different_dimensions() {
        Image big = aBigDog();
        Image small = aDog();

        Optional<Collision> potentialMatch = processor.detectCollision(big, small);

        assertThat(potentialMatch).isEmpty();
    }

    @Test
    void different_images() {
        Image cat = aCat();
        Image dog = aDog();

        Optional<Collision> potentialMatch = processor.detectCollision(cat, dog);

        assertThat(potentialMatch).isEmpty();
    }

    @Test
    void same_images() {
        Image a = aCat();
        Image b = aCat();

        Optional<Collision> potentialMatch = processor.detectCollision(a, b);

        assertThat(potentialMatch).isPresent();
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

    private static Image aCat() {
        return new StubImage("aCat", new int[] {0, 0, 0, 0});
    }

    private static Image aDog() {
        return new StubImage("aDog", new int[] {1, 1, 1, 1});
    }

    private static Image aBigDog() {
        return new StubImage("aBigDog", new int[] {1, 1, 1, 1, 1, 1, 1, 1});
    }

    private record StubImage(String name, int[] pixels) implements Image {

        @Override
        public int width() {
            return pixels.length / 2;
        }

        @Override
        public int height() {
            return pixels.length / 2;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}

