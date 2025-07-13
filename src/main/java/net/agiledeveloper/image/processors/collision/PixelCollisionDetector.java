package net.agiledeveloper.image.processors.collision;

import net.agiledeveloper.image.Image;

import java.io.IOException;

public class PixelCollisionDetector implements CollisionDetector {

    @Override
    public boolean collides(Image imageA, Image imageB) {
        int[] pixelsA = readPixels(imageA);
        int[] pixelsB = readPixels(imageB);

        for (int i = 0; i < pixelsA.length; i++) {
            if (pixelsA[i] != pixelsB[i]) {
                return false;
            }
        }

        return true;
    }

    private static int[] readPixels(Image image) {
        try {
            return image.pixels();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
