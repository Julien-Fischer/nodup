package net.agiledeveloper.processors;

import net.agiledeveloper.image.collision.CollisionDetector;
import net.agiledeveloper.image.collision.HashCollisionDetector;
import net.agiledeveloper.image.processors.BruteForceProcessor;

class BruteForceProcessorTest extends ImageProcessorTest {

    private static final CollisionDetector collisionDetector = new HashCollisionDetector();

    public BruteForceProcessorTest() {
        super(new BruteForceProcessor(collisionDetector));
    }

}

