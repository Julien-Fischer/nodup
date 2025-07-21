package net.agiledeveloper.nodup.image;

import java.nio.file.Path;

public interface ImageProvider {

    Image[] imagesAt(Path directory);

}
