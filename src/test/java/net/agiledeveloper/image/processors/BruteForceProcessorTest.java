package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.processors.collision.CollisionDetector;
import net.agiledeveloper.image.processors.collision.HashCollisionDetector;

public class BruteForceProcessorTest extends ImageProcessorTest {

    private static final CollisionDetector collisionDetector = new HashCollisionDetector();

    public BruteForceProcessorTest() {
        super(new BruteForceProcessor(collisionDetector));
    }

}

