package net.agiledeveloper.image.bin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;
import static net.agiledeveloper.App.COLLISION_BIN_NAME;

public class DateBin implements Bin {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("yyyy-MM-dd_HH-mm-ss");

    private Path rootDirectory;
    private Path currentDirectory;


    @Override
    public Path root() {
        lazyload(rootDirectory);
        return rootDirectory;
    }

    @Override
    public Path path() {
        lazyload(currentDirectory);
        return currentDirectory;
    }


    private void lazyload(Path directory) {
        if (directory == null) {
            try {
                initialize();
            } catch (IOException e) {
                throw new InitializationException(e);
            }
        }
    }

    private void initialize() throws IOException {
        String directoryName = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String userHome = System.getProperty("user.home");
        File newDir = new File(userHome, COLLISION_BIN_NAME);
        if (!newDir.exists() && !newDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + newDir.getAbsolutePath());
        }
        rootDirectory = newDir.toPath();
        currentDirectory = rootDirectory.resolve(directoryName);
        Files.createDirectories(currentDirectory);
    }

}
