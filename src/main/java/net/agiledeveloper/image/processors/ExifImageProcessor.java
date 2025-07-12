package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ExifImageProcessor extends BruteForceProcessor {

    @Override
    public Collection<Collision> detectCollisions(Collection<Image> images) {
        Map<Dimension, Collection<Image>> imagesByDimension = groupByDimension(images);

        return imagesByDimension.values().stream()
                .filter(list -> list.size() > 1)
                .flatMap(this::findCollisions)
                .toList();
    }


    private Stream<Collision> findCollisions(Collection<Image> images) {
        return super.detectCollisions(images).stream();
    }

    private static Map<Dimension, Collection<Image>> groupByDimension(Collection<Image> images) {
        var imagesByDimension = new HashMap<Dimension, Collection<Image>>();
        for (Image image : images) {
            var dimension = image.dimension();
            imagesByDimension.putIfAbsent(dimension, new ArrayList<>());
            imagesByDimension.get(dimension).add(image);
        }
        return imagesByDimension;
    }

}
