package net.agiledeveloper.image;

import java.io.IOException;
import java.nio.file.Path;

public interface Image {

    Path path();

    String format();

    long weight();

    int width();

    int height();

    default Dimension dimension() {
        return new Dimension(width(), height());
    }

    int[] pixels() throws IOException;

    default boolean hasSize(Image other) {
        return other.dimension().equals(dimension());
    }


    record Dimension(int width, int height) {

        @Override
        public String toString() {
            return "%sx%spx".formatted(width, height);
        }
    }

}
