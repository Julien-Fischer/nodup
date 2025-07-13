package net.agiledeveloper.image.processors.collision;

import net.agiledeveloper.image.Image;

public interface CollisionDetector {

    boolean collides(Image imageA, Image imageB);

}
