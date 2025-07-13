package net.agiledeveloper.processors;

import net.agiledeveloper.image.processors.BruteForceProcessor;
import net.agiledeveloper.image.processors.collision.CollisionDetector;
import net.agiledeveloper.image.processors.collision.HashCollisionDetector;

class BruteForceProcessorTest extends ImageProcessorTest {

    private static final CollisionDetector collisionDetector = new HashCollisionDetector();

    public BruteForceProcessorTest() {
        super(new BruteForceProcessor(collisionDetector));
    }

}

