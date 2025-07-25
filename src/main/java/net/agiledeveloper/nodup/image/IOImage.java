package net.agiledeveloper.nodup.image;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

public class IOImage implements Image {

    private final Path path;
    private String hash;
    private Dimension dimension;
    private String format;
    private Long weight;


    public IOImage(Path path) {
        this.path = path;
    }

    @Override
    public String hash() {
        return hash;
    }

    @Override
    public long weight() {
        if (weight == null) {
            weight = path.toFile().length();
        }
        return weight;
    }

    @Override
    public String format() {
        loadIfNecessary();
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
        loadIfNecessary();
        return dimension;
    }

    @Override
    public int[] pixels() throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        var pixels = new int[width() * height()];
        image.getRGB(0, 0, width(), height(), pixels, 0, width());
        return pixels;
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public String toString() {
        return path.getFileName().toString();
    }

    private void loadIfNecessary() {
        if (dimension == null) {
            loadMetadata();
        }
    }

    public void loadMetadata() {
        var file = path.toFile();
        try (ImageInputStream in = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                throw new Image.ReadException("No suitable ImageReader found for " + file);
            }
            ImageReader reader = readers.next();
            reader.setInput(in);
            var width = reader.getWidth(0);
            var height = reader.getHeight(0);
            format = reader.getFormatName();
            dimension = new Dimension(width, height);
            reader.dispose();
        } catch (IOException | IllegalArgumentException cause) {
            throw new Image.ReadException(cause);
        }
    }

}
