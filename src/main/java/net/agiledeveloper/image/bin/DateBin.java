package net.agiledeveloper.image.bin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;
import static net.agiledeveloper.App.COLLISION_BIN_NAME;

public class DateBin implements Bin {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("yyyy-MM-dd HH:mm:ss");

    private Path directory;


    @Override
    public Path path() {
        if (directory == null) {
            try {
                directory = initialize();
            } catch (IOException e) {
                throw new Bin.InitializationException(e);
            }
        }
        return directory;
    }

    private Path initialize() throws IOException {
        String directoryName = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String userHome = System.getProperty("user.home");
        File newDir = new File(userHome, COLLISION_BIN_NAME);
        if (!newDir.exists() && !newDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + newDir.getAbsolutePath());
        }
        return newDir
                .toPath()
                .resolve(directoryName);
    }

}
