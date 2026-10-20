package johnsmith.enchantingoverhauled.api.enchantment.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * A custom {@link LevelBasedValue} implementation that evaluates an inner {@link LevelBasedValue}
 * using the active world {@link net.minecraft.world.Difficulty} rather than the raw enchantment level.
 * <p>
 * Maps Peaceful and Easy to level {@code 1}, Normal to level {@code 2}, and Hard to level {@code 3}.
 * This allows AI spell fields (such as Obliteration's explosion radius) to scale dynamically with world difficulty.
 *
 * @param value The inner {@link LevelBasedValue} evaluated against the difficulty-mapped level ({@code 1}, {@code 2}, or {@code 3}).
 * @see DifficultyContext
 * @see LevelBasedValues#DIFFICULTY
 */
public record DifficultyBasedValue(
        LevelBasedValue value
) implements LevelBasedValue {

    /**
     * The {@link MapCodec} responsible for serializing and deserializing {@link DifficultyBasedValue} instances.
     */
    public static final MapCodec<DifficultyBasedValue> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    LevelBasedValue.CODEC.fieldOf("value").forGetter(DifficultyBasedValue::value)
            ).apply(instance, DifficultyBasedValue::new)
    );

    /**
     * Calculates the value by querying {@link DifficultyContext#getDifficulty()} and passing the
     * corresponding difficulty tier ({@code 1} for Peaceful/Easy, {@code 2} for Normal, {@code 3} for Hard)
     * into the wrapped {@link #value}.
     *
     * @param level The nominal enchantment level (ignored in favor of world difficulty).
     * @return The calculated float value for the current world difficulty.
     */
    @Override
    public float calculate(int level) {
        return switch (DifficultyContext.getDifficulty()) {
            case PEACEFUL -> value.calculate(1);
            case EASY -> value.calculate(1);
            case NORMAL -> value.calculate(2);
            case HARD -> value.calculate(3);
        };
    }

    /**
     * Retrieves the {@link MapCodec} for this level-based value type.
     *
     * @return The {@link #CODEC} instance.
     */
    @Override
    public MapCodec<DifficultyBasedValue> codec() {
        return CODEC;
    }
}