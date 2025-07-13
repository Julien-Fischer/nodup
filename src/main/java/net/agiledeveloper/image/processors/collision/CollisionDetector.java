package net.agiledeveloper.image.processors.collision;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.processors.ImageProcessor.Collision;

import java.util.Optional;

public interface CollisionDetector {

    Optional<Collision> of(Image imageA, Image imageB);

}
