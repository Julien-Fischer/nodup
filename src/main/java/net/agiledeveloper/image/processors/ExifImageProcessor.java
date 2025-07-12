package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ExifImageProcessor implements ImageProcessor {

    @Override
    public Collection<Collision> detectCollisions(Image... images) {
        return List.of();
    }

    @Override
    public Optional<Collision> detectCollision(Image imageA, Image imageB) {
        return Optional.empty();
    }

}
