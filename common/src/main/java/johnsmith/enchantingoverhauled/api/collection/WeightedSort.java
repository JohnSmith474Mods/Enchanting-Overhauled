package johnsmith.enchantingoverhauled.api.collection;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * A utility record and helper for performing weighted random sorting (skewed shuffling)
 * over a collection of elements.
 * <p>
 * Each element is paired with a randomized weight score and sorted in descending order,
 * allowing higher-weight elements (such as common enchantments) to statistically appear earlier
 * in the resulting list while still preserving randomness.
 *
 * @param <T>    The type of element being weighted and sorted.
 * @param item   The underlying element instance.
 * @param weight The computed randomized weight value used for sorting.
 */
public record WeightedSort<T>(
        T item,
        double weight
) implements Comparable<WeightedSort<T>> {

    /**
     * Compares this weighted entry to another in descending order of weight so that
     * higher weights are ordered first when sorted via {@link Collections#sort(List)}.
     *
     * @param other The other {@code WeightedSort} entry to compare against.
     * @return A negative integer, zero, or a positive integer if this entry's weight is
     *         greater than, equal to, or less than the other entry's weight.
     */
    @Override
    public int compareTo(WeightedSort<T> other) {
        return Double.compare(other.weight, this.weight);
    }

    /**
     * Produces a new list containing the elements of {@code originalList} shuffled with a bias
     * toward elements with higher integer weights.
     * <p>
     * Elements with a weight less than or equal to {@code 0} are assigned {@link Double#NEGATIVE_INFINITY}
     * and placed at the end of the list. Elements with positive weights have their weight divided by
     * a uniform random factor in {@code (0.0, 1.0]} and are sorted in descending order.
     *
     * @param <T>             The element type in the collection.
     * @param originalList    The source collection of elements to shuffle.
     * @param weightExtractor A function that extracts the non-negative integer base weight for each element.
     * @param random          The {@link RandomSource} used to perturb weights.
     * @return A new {@link List} containing the elements ordered by their randomized weights.
     */
    public static <T> List<T> skewedShuffle(Collection<T> originalList, ToIntFunction<T> weightExtractor, RandomSource random) {
        List<WeightedSort<T>> weightedList = new ArrayList<>(originalList.size());

        for (T item : originalList) {
            double weight = weightExtractor.applyAsInt(item);

            if (weight <= 0) {
                weightedList.add(new WeightedSort<>(item, Double.NEGATIVE_INFINITY));
                continue;
            }

            double randomFactor = 1.0 - random.nextDouble();
            weight /= randomFactor;
            weightedList.add(new WeightedSort<>(item, weight));
        }

        Collections.sort(weightedList);

        return weightedList.stream()
                .map(WeightedSort::item)
                .collect(Collectors.toList());
    }
}