package net.agiledeveloper.image.processors;

import net.agiledeveloper.image.Image;
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
        Map<BucketKey, PotentialCollision> potentialCollisions = groupByDiscriminator(images)
                .filter(atLeastOnePotentialCollision())
                .collect(toPotentialCollision());

        return findActualCollisions(potentialCollisions);
    }


    private List<Collision> findActualCollisions(Map<BucketKey, PotentialCollision> potentialCollisions) {
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

    private int countTotal(Map<BucketKey, PotentialCollision> potentialCollisions) {
        return potentialCollisions.values().stream()
                .mapToInt(potentialCollision -> potentialCollision.count)
                .sum();
    }

    private Map<BucketKey, Integer> toFrequencyMap(Map<BucketKey, PotentialCollision> potentialCollisionMap) {
        return potentialCollisionMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> entry.getValue().count()
                ));
    }

    private void logSummary(int total, Map<BucketKey, PotentialCollision> potentialCollisions) {
        logger.info(() -> format(
                "Found %s potential collisions over %s buckets:",
                total, potentialCollisions.size())
        );
    }

    private void logBuckets(Map<BucketKey, Integer> map) {
        map.entrySet().stream()
                .sorted(Entry.<BucketKey, Integer>comparingByValue().reversed())
                .forEach(printBucket());
    }


    private Stream<Collision> findCollisions(PotentialCollision potentialCollision) {
        return super.detectCollisions(potentialCollision.images()).stream();
    }

    private static Stream<Entry<BucketKey, Collection<Image>>> groupByDiscriminator(Collection<Image> images) {
        var imagesByDiscriminator = new HashMap<BucketKey, Collection<Image>>();
        for (Image image : images) {
            var key = new BucketKey(image);
            imagesByDiscriminator.putIfAbsent(key, new ArrayList<>());
            imagesByDiscriminator.get(key).add(image);
        }
        return imagesByDiscriminator.entrySet().stream();
    }

    private static Predicate<Entry<BucketKey, Collection<Image>>> atLeastOnePotentialCollision() {
        return entry -> entry.getValue().size() > 1;
    }

    private Consumer<Entry<BucketKey, Integer>> printBucket() {
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

    private static StringBuilder printPotentialCollision(StringBuilder stringBuilder, Entry<BucketKey, ?> entry) {
        return stringBuilder
                .append(entry.getValue())
                .append(": ")
                .append(entry.getKey());
    }

    private static Collector<
            Entry<BucketKey, Collection<Image>>,
            ?,
            Map<BucketKey, PotentialCollision>
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

    private static class BucketKey {

        private final String value;
        private final Image.Dimension dimension;


        private BucketKey(Image image) {
            this.dimension = image.dimension();
            this.value = image.dimension() + "-" + image.format();
        }


        @Override
        public boolean equals(Object o) {
            if (!(o instanceof BucketKey bucketKey)) return false;
            return Objects.equals(value, bucketKey.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return dimension.toString();
        }
    }

}
