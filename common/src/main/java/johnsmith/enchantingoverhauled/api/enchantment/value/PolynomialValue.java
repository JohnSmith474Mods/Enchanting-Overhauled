package johnsmith.enchantingoverhauled.api.enchantment.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

public record PolynomialValue(
        float scale,
        float power,
        float offset,
        float levelOffset
) implements LevelBasedValue {

    public static final MapCodec<PolynomialValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(PolynomialValue::scale),
            Codec.FLOAT.optionalFieldOf("power", 1.0f).forGetter(PolynomialValue::power),
            Codec.FLOAT.optionalFieldOf("offset", 0.0f).forGetter(PolynomialValue::offset),
            Codec.FLOAT.optionalFieldOf("level_offset", 0.0f).forGetter(PolynomialValue::levelOffset)
    ).apply(instance, PolynomialValue::new));

    @Override
    public float calculate(int level) {
        return (float) (offset + (scale * Math.pow(level + levelOffset, power)));
    }

    @Override
    public @NotNull MapCodec<? extends LevelBasedValue> codec() {
        return CODEC;
    }
}