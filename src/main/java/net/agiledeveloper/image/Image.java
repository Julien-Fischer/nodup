package net.agiledeveloper.image;

import java.awt.*;
import java.io.IOException;

public interface Image {

    int width();

    int height();

    default Dimension dimension() {
        return new Dimension(width(), height());
    }

    int[] pixels() throws IOException;

    default boolean hasSize(Image other) {
        return (
                width() == other.width() &&
                height() == other.height()
        );
    }

}
