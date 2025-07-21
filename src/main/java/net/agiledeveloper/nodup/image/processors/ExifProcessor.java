package net.agiledeveloper.nodup.image.processors;

import net.agiledeveloper.nodup.App;
import net.agiledeveloper.nodup.image.Image;
import net.agiledeveloper.nodup.image.Image.Dimension;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

public class ExifProcessor extends BucketProcessor {

    @Override
    public Collection<Collision> detectCollisions(Collection<Image> images) {
        Map<Discriminator, PotentialCollision> potentialCollisions = groupByDiscriminator(images)
                .filter(atLeastOnePotentialCollision())
                .collect(toPotentialCollision());

        return findActualCollisions(potentialCollisions);
    }


    private List<Collision> findActualCollisions(Map<Discriminator, PotentialCollision> potentialCollisions) {
        logger.setLevel(App.LOG_LEVEL);
        var total = countTotal(potentialCollisions);
        logSummary(total, potentialCollisions);
        logBuckets(toFrequencyMap(potentialCollisions));

        var processedImages = new AtomicInteger(0);
        return potentialCollisions.entrySet()
                .parallelStream()
                .flatMap(entry -> {
                    int progressIndex = processedImages.getAndAdd(entry.getValue().count);
                    double progress = (double) progressIndex / total * 100;
                    var stringBuilder = new StringBuilder();
                    logger.info(() -> String.format("%05.2f%%", progress));
                    logger.fine(() -> printPotentialCollision(stringBuilder, entry).toString());
                    return findCollisions(entry.getValue());
                })
                .toList();
    }


    private int countTotal(Map<Discriminator, PotentialCollision> potentialCollisions) {
        return potentialCollisions.values().stream()
                .mapToInt(potentialCollision -> potentialCollision.count)
                .sum();
    }

    private Map<Discriminator, Integer> toFrequencyMap(Map<Discriminator, PotentialCollision> potentialCollisionMap) {
        return potentialCollisionMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> entry.getValue().count()
                ));
    }

    private void logSummary(int total, Map<Discriminator, PotentialCollision> potentialCollisions) {
        logger.info(() -> format(
                "Found %s potential collisions over %s buckets:",
                total, potentialCollisions.size())
        );
    }

    private void logBuckets(Map<Discriminator, Integer> map) {
        map.entrySet().stream()
                .sorted(Entry.<Discriminator, Integer>comparingByValue().reversed())
                .forEach(printBucket());
    }


    private Stream<Collision> findCollisions(PotentialCollision potentialCollision) {
        return super.detectCollisions(potentialCollision.images()).stream();
    }

    private Stream<Entry<Discriminator, Collection<Image>>> groupByDiscriminator(Collection<Image> images) {
        var imagesByDiscriminator = new HashMap<Discriminator, Collection<Image>>();
        for (Image image : images) {
            try {
                var key = new Discriminator(image);
                imagesByDiscriminator.putIfAbsent(key, new ArrayList<>());
                imagesByDiscriminator.get(key).add(image);
            } catch (Image.ReadException exception) {
                logger.fine("Ignoring %s: not an image file".formatted(image.path()));
            }
        }
        return imagesByDiscriminator.entrySet().stream();
    }

    private static Predicate<Entry<Discriminator, Collection<Image>>> atLeastOnePotentialCollision() {
        return entry -> entry.getValue().size() > 1;
    }

    private Consumer<Entry<Discriminator, Integer>> printBucket() {
        return entry -> {
            var stringBuilder = new StringBuilder();
            logger.finer(() -> stringBuilder
                    .append("    ")
                    .append(entry.getValue())
                    .append(": ")
                    .append(entry.getKey().displayName())
                    .toString()
            );
        };
    }

    private static StringBuilder printPotentialCollision(StringBuilder stringBuilder, Entry<Discriminator, ?> entry) {
        return stringBuilder
                .append(entry.getValue())
                .append(": ")
                .append(entry.getKey());
    }

    private static Collector<
            Entry<Discriminator, Collection<Image>>,
            ?,
            Map<Discriminator, PotentialCollision>
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

    private static class Discriminator {

        private final String value;
        private final Dimension dimension;


        private Discriminator(Image image) {
            this.dimension = image.dimension();
            this.value = discriminateFields(image);
        }


        public String displayName() {
            return dimension.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Discriminator discriminator)) return false;
            return Objects.equals(value, discriminator.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return value;
        }

        private static String discriminateFields(Image image) {
            return image.dimension() + "-" + image.format() + " " + image.weight();
        }
    }

}
