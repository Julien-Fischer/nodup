package net.agiledeveloper.image;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

public class IOImage implements Image {

    private final Path path;
    private int[] pixels;
    private Dimension dimension;
    private String format;


    public IOImage(Path path) {
        this.path = path;
    }


    @Override
    public String format() {
        return format;
    }

    @Override
    public int width() {
        return dimension().width();
    }

    @Override
    public int height() {
        return dimension().height();
    }

    @Override
    public Dimension dimension() {
        if (dimension == null) {
            loadDimensions();
        }
        return dimension;
    }

    @Override
    public int[] pixels() throws IOException {
        if (pixels == null) {
            BufferedImage image = ImageIO.read(path.toFile());
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

    public void loadDimensions() {
        var file = path.toFile();
        try (ImageInputStream in = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                throw new RuntimeException("No suitable ImageReader found for " + file);
            }
            ImageReader reader = readers.next();
            reader.setInput(in);
            var width = reader.getWidth(0);
            var height = reader.getHeight(0);
            format = reader.getFormatName();
            dimension = new Dimension(width, height);
            reader.dispose();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
