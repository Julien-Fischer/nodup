package net.agiledeveloper.image.bin;

import net.agiledeveloper.App.Action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

import static net.agiledeveloper.App.Action.SCAN;
import static net.agiledeveloper.App.logger;

public class Bin {

    private final PathProvider pathProvider;


    public Bin(PathProvider pathProvider) {
        this.pathProvider = pathProvider;
    }


    public Path root() {
        return pathProvider.root();
    }

    public void accept(Action action, Collection<Path> files) throws IOException {
        if (action != SCAN) {
            var currentBinDirectory = pathProvider.currentBin();
            createDirectory(currentBinDirectory);
            logger.info(() -> "About to [%s] %s duplicates to %s:".formatted(action, files.size(), currentBinDirectory));
            for (var sourcePath : files) {
                Path targetPath = currentBinDirectory.resolve(sourcePath.getFileName());
                performAction(action, sourcePath, targetPath);
                logger.fine(() -> "File moved to: " + targetPath);
            }
            logger.info(() -> "Done [%s] %s duplicates to %s:".formatted(action, files.size(), currentBinDirectory));
        }
    }


    private void performAction(Action action, Path sourcePath, Path targetPath) throws IOException {
        switch (action) {
            case MOVE -> Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            case COPY -> Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            default -> throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }

    private void createDirectory(Path directoryPath) throws IOException {
        String userHome = System.getProperty("user.home");
        var newDir = new File(userHome, root().toString());
        if (!newDir.exists() && !newDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + newDir.getAbsolutePath());
        }
        Files.createDirectories(directoryPath);
    }


    public interface PathProvider {

        Path root();

        Path currentBin();
    }

}

