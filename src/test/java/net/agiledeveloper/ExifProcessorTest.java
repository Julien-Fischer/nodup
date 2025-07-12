package net.agiledeveloper;

import net.agiledeveloper.image.collision.CollisionDetector;
import net.agiledeveloper.image.collision.HashCollisionDetector;
import net.agiledeveloper.image.processors.ExifImageProcessor;

class ExifProcessorTest extends ImageProcessorTest {

    private static final CollisionDetector collisionDetector = new HashCollisionDetector();

    public ExifProcessorTest() {
        super(new ExifImageProcessor(collisionDetector));
    }

}

