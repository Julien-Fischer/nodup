package net.agiledeveloper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class IOImage implements Image {

    private final Path path;
    private int[] pixels;
    private Integer width;
    private Integer height;

    public IOImage(Path path) {
        this.path = path;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int[] pixels() throws IOException {
        if (pixels == null) {
            BufferedImage image = ImageIO.read(path.toFile());
            width = image.getWidth();
            height = image.getHeight();
            pixels = new int[width() * height()];
            image.getRGB(0, 0, width(), height(), pixels, 0, width());
        }
        return pixels;
    }

    public Path path() {
        return path;
    }

    @Override
    public String toString() {
        return path.getFileName().toString();
    }

}
