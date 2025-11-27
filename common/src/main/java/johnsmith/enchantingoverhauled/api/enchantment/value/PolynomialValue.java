package johnsmith.enchantingoverhauled.api.enchantment.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

/**
 * A specialized implementation of {@link LevelBasedValue} that calculates a final value
 * using a generalized polynomial function.
 * <p>
 * The formula used is: {@code value = offset + (scale * (level + levelOffset)^power)}.
 * This allows for custom growth curves, such as linear, quadratic, exponential, or fractional
 * scaling, by adjusting the parameters.
 *
 * @param scale The coefficient that multiplies the result of the power calculation. Defaults to {@code 1.0f}.
 * @param power The exponent applied to the level calculation. Defaults to {@code 1.0f} (linear).
 * @param offset A constant value added to the final result. Defaults to {@code 0.0f}.
 * @param levelOffset A constant value added to the enchantment level before the power calculation. Defaults to {@code 0.0f}.
 */
public record PolynomialValue(
        float scale,
        float power,
        float offset,
        float levelOffset
) implements LevelBasedValue {
    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     * All fields are optional and default to linear scaling (scale=1.0, power=1.0, offset=0.0, levelOffset=0.0).
     */
    public static final MapCodec<PolynomialValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(PolynomialValue::scale),
            Codec.FLOAT.optionalFieldOf("power", 1.0f).forGetter(PolynomialValue::power),
            Codec.FLOAT.optionalFieldOf("offset", 0.0f).forGetter(PolynomialValue::offset),
            Codec.FLOAT.optionalFieldOf("level_offset", 0.0f).forGetter(PolynomialValue::levelOffset)
    ).apply(instance, PolynomialValue::new));

    /**
     * Calculates the value using the defined polynomial formula: {@code offset + (scale * (level + levelOffset)^power)}.
     *
     * @param level The level of the enchantment to use in the calculation.
     * @return The calculated float value.
     */
    @Override
    public float calculate(int level) {
        return (float) (offset + (scale * Math.pow(level + levelOffset, power)));
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