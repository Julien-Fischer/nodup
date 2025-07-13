package net.agiledeveloper.image.bin;

import java.nio.file.Path;

public interface Bin {

    Path path();


    class InitializationException extends RuntimeException {

        public InitializationException(String message) {
            super(message);
        }

        public InitializationException(Exception cause) {
            super(cause);
        }

    }

}
