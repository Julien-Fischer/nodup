package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.collision.CollisionDetector;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExifImageProcessor extends BruteForceProcessor {

    public ExifImageProcessor(CollisionDetector collisionDetector) {
        super(collisionDetector);
    }


    @Override
    public Collection<Collision> detectCollisions(Collection<Image> images) {
        Map<Dimension, Collection<Image>> imagesByDimension = groupByDimension(images);

        System.out.println(printCount(imagesByDimension));

        return imagesByDimension.values().stream()
                .filter(list -> list.size() > 1)
                .flatMap(this::findCollisions)
                .toList();
    }

    private Map<Dimension, Integer> count(Map<Dimension, Collection<Image>> imagesByDimension) {
        return imagesByDimension.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size()
                ));
    }

    private String printCount(Map<Dimension, Collection<Image>> imagesByDimension) {
        return "" + count(imagesByDimension);
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
