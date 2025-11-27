package johnsmith.enchantingoverhauled.api.enchantment.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.enchantment.LevelBasedValue;

import org.jetbrains.annotations.NotNull;

/**
 * A specialized implementation of {@link LevelBasedValue} that calculates the negative
 * additive inverse of the value returned by an inner {@code input} value provider.
 * <p>
 * This is primarily useful for creating diminishing or negative effects (e.g., charge time reduction)
 * where the underlying value calculation naturally yields a positive number.
 *
 * @param input The inner {@link LevelBasedValue} whose calculated result will be negated.
 */
public record NegateValue(
        LevelBasedValue input
) implements LevelBasedValue {
    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     * It maps the nested {@code "value"} field to a generic {@link LevelBasedValue}.
     */
    public static final MapCodec<NegateValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("value").forGetter(NegateValue::input)
    ).apply(instance, NegateValue::new));

    /**
     * Calculates the value by retrieving the result from the nested {@code input} provider
     * and inverting its sign (multiplying by -1).
     *
     * @param level The level of the enchantment.
     * @return The negated float value.
     */
    @Override
    public float calculate(int level) {
        return -this.input.calculate(level);
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