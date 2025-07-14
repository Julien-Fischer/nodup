package net.agiledeveloper;

import java.nio.file.Path;

public interface DirectoryOpener {

    void open(Path directory) throws UnsupportedOperationException;


    class OpenException extends RuntimeException {

        public OpenException(String message) {
            super(message);
        }

        public OpenException(String message, Exception cause) {
            super(message, cause);
        }

        public static OpenException forDirectory(Path directory, Exception cause) {
            return new OpenException("Could not open directory %s".formatted(directory), cause);
        }
    }

}
