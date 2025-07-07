package net.agiledeveloper;

public interface Image {

    int width();

    int height();

    int[] pixels();

    default boolean hasSize(Image other) {
        return (
                width() == other.width() &&
                height() == other.height()
        );
    }

}
