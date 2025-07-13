package net.agiledeveloper.image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SimpleImageProvider implements ImageProvider {

    @Override
    public Image[] imagesAt(String directory) {
        Path dir = Paths.get(directory);
        try {
            return getFilesOnly(dir).stream()
                    .map(IOImage::new)
                    .toArray(Image[]::new);
        } catch (IOException e) {
            throw new Image.ReadException(e);
        }
    }

    private static List<Path> getFilesOnly(Path directory) throws IOException {
        try (var stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .toList();
        }
    }

}
