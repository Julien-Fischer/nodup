package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.Image.Dimension;
import net.agiledeveloper.image.collision.CollisionDetector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExifImageProcessor extends BruteForceProcessor {

    public ExifImageProcessor(CollisionDetector collisionDetector) {
        super(collisionDetector);
    }


    @Override
    public Collection<Collision> detectCollisions(Collection<Image> images) {
        Map<Dimension, Collection<Image>> potentialCollisions = groupByDimension(images);

        logger.info(() -> printCount(potentialCollisions));

        return potentialCollisions.values().stream()
                .flatMap(this::findCollisions)
                .toList();
    }

    private Map<Dimension, Integer> count(Map<Dimension, Collection<Image>> imagesByDimension) {
        return imagesByDimension.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> entry.getValue().size()
                ));
    }

    private String printCount(Map<Dimension, Collection<Image>> imagesByDimension) {
        return accumulate(count(imagesByDimension));
    }

    private String accumulate(Map<Dimension, Integer> map) {
        var stringBuilder = new StringBuilder();
        map.entrySet().stream()
                .sorted(Entry.<Dimension, Integer>comparingByValue().reversed())
                .forEach(printEntry(stringBuilder));
        return stringBuilder.toString();
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
        return imagesByDimension.entrySet().stream()
                .filter(atLeastOnePotentialCollision())
                .collect(identity());
    }

    private static Predicate<Entry<Dimension, Collection<Image>>> atLeastOnePotentialCollision() {
        return entry -> entry.getValue().size() > 1;
    }

    private static <K, V> Collector<Entry<K, V>, ?, Map<K, V>> identity() {
        return Collectors.toMap(
                Entry::getKey,
                Entry::getValue
        );
    }

    private static Consumer<Entry<Dimension, Integer>> printEntry(StringBuilder stringBuilder) {
        return entry -> stringBuilder
                .append(entry.getValue())
                .append(": ")
                .append(entry.getKey())
                .append("\n");
    }

}
