package net.agiledeveloper.image.bin;

import net.agiledeveloper.App;
import net.agiledeveloper.image.bin.Bin.PathProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;

public class DatePathProvider implements PathProvider {

    public static final Path BIN_ROOT = Paths.get(System.getProperty("user.home"), App.ROOT_DIR, "bin");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("yyyy-MM-dd_HH-mm-ss");


    @Override
    public Path root() {
        return BIN_ROOT;
    }

    @Override
    public Path currentBin() {
        return root().resolve(LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }

}
