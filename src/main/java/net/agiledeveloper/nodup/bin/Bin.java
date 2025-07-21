package net.agiledeveloper.nodup.bin;

import net.agiledeveloper.nodup.App.Action;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static net.agiledeveloper.nodup.App.Action.SCAN;
import static net.agiledeveloper.nodup.App.logger;

public class Bin {

    private final PathProvider pathProvider;


    public Bin(PathProvider pathProvider) {
        this.pathProvider = pathProvider;
    }


    public Path root() {
        return pathProvider.root();
    }

    public void accept(Action action, Collection<Path> files) throws BinException {
        if (action != SCAN) {
            try {
                tryExecuting(action, files);
            } catch (IOException cause) {
                throw new BinException(cause);
            }
        }
    }

    public List<Path> directories() throws BinException {
        File[] files = root().toFile().listFiles(File::isDirectory);
        return files == null ? emptyList() : stream(files).map(File::toPath).toList();
    }

    public boolean isEmpty() throws BinException {
        return directories().isEmpty();
    }

    public void clear() throws BinException {
        try {
            Files.walkFileTree(root(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException cause) {
            throw new BinException(cause);
        }
    }


    private void tryExecuting(Action action, Collection<Path> files) throws IOException {
        var currentBinDirectory = pathProvider.currentBin();
        Files.createDirectories(currentBinDirectory);
        logger.info(() -> "About to [%s] %s duplicates to %s:".formatted(action, files.size(), currentBinDirectory));
        for (var sourcePath : files) {
            Path targetPath = currentBinDirectory.resolve(sourcePath.getFileName());
            performAction(action, sourcePath, targetPath);
            logger.fine(() -> "File moved to: " + targetPath);
        }
        logger.info(() -> "Done [%s] %s duplicates to %s:".formatted(action, files.size(), currentBinDirectory));
    }


    private void performAction(Action action, Path sourcePath, Path targetPath) throws IOException {
        switch (action) {
            case MOVE -> Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            case COPY -> Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            default -> throw new BindException("Unsupported action: " + action);
        }
    }


    public interface PathProvider {

        Path root();

        Path currentBin();

    }

    public static class BinException extends RuntimeException {

        public BinException(Throwable cause) {
            super(cause);
        }

    }

}

