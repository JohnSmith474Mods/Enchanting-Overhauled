package johnsmith.enchantingoverhauled.api.enchantment.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.enchantment.LevelBasedValue;

import org.jetbrains.annotations.NotNull;

/**
 * A specialized implementation of {@link LevelBasedValue} that calculates a total value
 * based on an arithmetic progression where the increment decreases (diminishes) for each
 * subsequent level until a defined minimum value is reached (the "floor").
 * <p>
 * The value added at level N is calculated as: {@code max(base - (N - 1) * decrement, minimum)}.
 * The final result is the sum of these diminishing terms up to the given level.
 *
 * @param base      The initial value added at the first level (Level 1).
 * @param decrement The amount by which the value added is reduced for each subsequent level.
 * @param minimum   The floor value. The added value per level will never drop below this amount.
 */
public record DiminishingReturnsValue(
        float base,
        float decrement,
        float minimum
) implements LevelBasedValue {
    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     */
    public static final MapCodec<DiminishingReturnsValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("base").forGetter(DiminishingReturnsValue::base),
            Codec.FLOAT.fieldOf("decrement").forGetter(DiminishingReturnsValue::decrement),
            Codec.FLOAT.fieldOf("minimum").forGetter(DiminishingReturnsValue::minimum)
    ).apply(instance, DiminishingReturnsValue::new));

    /**
     * Calculates the cumulative total value up to the given enchantment level using diminishing returns.
     * <p>
     * For efficiency, this method uses the formula for the sum of an arithmetic progression
     * for the initial terms (where reduction occurs) and then adds the constant floor value
     * for any remaining terms. If the decrement is zero or negative, it falls back to an
     * iterative calculation to ensure correct non-diminishing behavior.
     *
     * @param level The enchantment level for which to calculate the cumulative value.
     * @return The total float value. Returns 0 if {@code level <= 0}.
     */
    @Override
    public float calculate(int level) {
        if (level <= 0) return 0;

        if (this.decrement <= 0.0001f) {
            if (this.decrement == 0) {
                return level * Math.max(this.base, this.minimum);
            }
            return calculateIterative(level);
        }

        float limit = (this.base - this.minimum) / this.decrement;
        int termCount = Math.max(1, (int) Math.floor(limit) + 1);

        int count = Math.min(level, termCount);

        // Sum of arithmetic progression: S_n = n/2 * (2a + (n-1)d) where a=base, d=-decrement
        float sum = count * (2 * this.base - (count - 1) * this.decrement) / 2f;

        if (level > count) {
            int remaining = level - count;
            sum += remaining * this.minimum;
        }

        return sum;
    }

    /**
     * A fallback calculation method that iteratively sums up the value for each level.
     * <p>
     * This is used for edge cases where the {@code decrement} is non-positive,
     * as the optimized formula relies on a strictly positive {@code decrement}.
     *
     * @param level The enchantment level.
     * @return The total calculated float value.
     */
    private float calculateIterative(int level) {
        float currentBase = this.base;
        float total = 0;
        for (int i = 0; i < level; i++) {
            total += currentBase;
            currentBase = Math.max(currentBase - this.decrement, this.minimum);
        }
        return total;
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@link MapCodec} for this class.
     */
    @Override
    public @NotNull MapCodec<? extends LevelBasedValue> codec() {
        return CODEC;
    }
}