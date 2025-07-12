package net.agiledeveloper.image;

import java.io.IOException;

public interface Image {

    int width();

    int height();

    int[] pixels() throws IOException;

    default boolean hasSize(Image other) {
        return (
                width() == other.width() &&
                height() == other.height()
        );
    }

}
