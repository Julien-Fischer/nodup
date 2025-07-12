package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.Image.Dimension;
import net.agiledeveloper.image.collision.CollisionDetector;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

public class ExifImageProcessor extends BruteForceProcessor {

    public ExifImageProcessor(CollisionDetector collisionDetector) {
        super(collisionDetector);
    }


    @Override
    public Collection<Collision> detectCollisions(Collection<Image> images) {
        Map<Dimension, PotentialCollision> potentialCollisions = groupByDimension(images)
                .filter(atLeastOnePotentialCollision())
                .collect(toPotentialCollision());

        return findActualCollisions(potentialCollisions);
    }


    private List<Collision> findActualCollisions(Map<Dimension, PotentialCollision> potentialCollisions) {
        var total = countTotal(potentialCollisions);
        logSummary(total, potentialCollisions);
        logBuckets(toFrequencyMap(potentialCollisions));

        int i = 0;
        List<Collision> collisions = new ArrayList<>();
        for (var entry : potentialCollisions.entrySet()) {
            var progress = printProgress(i, total, "Potential collision");
            var stringBuilder = new StringBuilder();
            logger.info(() -> "%s: %s".formatted(progress, printPotentialCollision(stringBuilder, entry)));
            var found = findCollisions(entry.getValue());
            collisions.addAll(found.toList());
            i += entry.getValue().count;
        }
        return collisions;
    }

    private int countTotal(Map<Dimension, PotentialCollision> potentialCollisions) {
        return potentialCollisions.values().stream()
                .mapToInt(potentialCollision -> potentialCollision.count)
                .sum();
    }

    private Map<Dimension, Integer> toFrequencyMap(Map<Dimension, PotentialCollision> potentialCollisionMap) {
        return potentialCollisionMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> entry.getValue().count()
                ));
    }

    private void logSummary(int total, Map<Dimension, PotentialCollision> potentialCollisions) {
        logger.info(() -> format(
                "Found %s potential collisions over %s buckets:",
                total, potentialCollisions.size())
        );
    }

    private void logBuckets(Map<Dimension, Integer> map) {
        map.entrySet().stream()
                .sorted(Entry.<Dimension, Integer>comparingByValue().reversed())
                .forEach(printBucket());
    }


    private Stream<Collision> findCollisions(PotentialCollision potentialCollision) {
        return super.detectCollisions(potentialCollision.images()).stream();
    }

    private static Stream<Entry<Dimension, Collection<Image>>> groupByDimension(Collection<Image> images) {
        var imagesByDimension = new HashMap<Dimension, Collection<Image>>();
        for (Image image : images) {
            var dimension = image.dimension();
            imagesByDimension.putIfAbsent(dimension, new ArrayList<>());
            imagesByDimension.get(dimension).add(image);
        }
        return imagesByDimension.entrySet().stream();
    }

    private static Predicate<Entry<Dimension, Collection<Image>>> atLeastOnePotentialCollision() {
        return entry -> entry.getValue().size() > 1;
    }

    private Consumer<Entry<Dimension, Integer>> printBucket() {
        return entry -> {
            var stringBuilder = new StringBuilder();
            logger.fine(() -> stringBuilder
                    .append("    ")
                    .append(entry.getValue())
                    .append(": ")
                    .append(entry.getKey())
                    .toString()
            );
        };
    }

    private static StringBuilder printPotentialCollision(StringBuilder stringBuilder, Entry<Dimension, ?> entry) {
        return stringBuilder
                .append(entry.getValue())
                .append(": ")
                .append(entry.getKey());
    }

    private static Collector<
            Entry<Dimension, Collection<Image>>,
            ?,
            Map<Dimension, PotentialCollision>
    > toPotentialCollision() {
        return Collectors.toMap(
                Entry::getKey,
                entry -> new PotentialCollision(entry.getValue())
        );
    }

    private record PotentialCollision(Collection<Image> images, int count) {

        private PotentialCollision(Collection<Image> images) {
            this(images, images.size());
        }

        @Override
        public int count() {
            return images.size();
        }

        @Override
        public String toString() {
            return "" + count;
        }
    }

}
