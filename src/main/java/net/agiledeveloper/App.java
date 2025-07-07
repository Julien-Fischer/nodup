package net.agiledeveloper;

import net.agiledeveloper.ImageProcessor.Collision;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

public class App {

    public static void main(String[] args) {
        requireValidArguments(args);

        String directory = args[0];
        Image[] images = at(directory);
        Collection<Collision> collisions = ImageProcessor.detectCollisions(images);
        for (Collision collision : collisions) {
            System.out.println(collision);
        }
    }

    private static void requireValidArguments(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Missing required arguments");
        }
    }

    private static Image[] at(String directory) {
        Path dir = Paths.get(directory);
        try {
            return getFilesOnly(dir).stream()
                    .map(IOImage::new)
                    .toArray(Image[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
