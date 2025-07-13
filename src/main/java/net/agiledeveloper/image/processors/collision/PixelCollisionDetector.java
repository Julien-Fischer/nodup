package net.agiledeveloper.image.processors.collision;

import net.agiledeveloper.image.Image;

import java.io.IOException;
import java.util.Optional;

import static net.agiledeveloper.image.processors.ImageProcessor.Collision;

public class PixelCollisionDetector implements CollisionDetector {

    @Override
    public Optional<Collision> of(Image imageA, Image imageB) {
        int[] pixelsA = readPixels(imageA);
        int[] pixelsB = readPixels(imageB);

        for (int i = 0; i < pixelsA.length; i++) {
            if (pixelsA[i] != pixelsB[i]) {
                return Optional.empty();
            }
        }

        return Optional.of(new Collision(imageA, imageB));
    }

    private static int[] readPixels(Image image) {
        try {
            return image.pixels();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
