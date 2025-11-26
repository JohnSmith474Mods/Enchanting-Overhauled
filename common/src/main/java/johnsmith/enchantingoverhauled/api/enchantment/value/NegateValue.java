package johnsmith.enchantingoverhauled.api.enchantment.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

public record NegateValue(LevelBasedValue input) implements LevelBasedValue {

    public static final MapCodec<NegateValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("value").forGetter(NegateValue::input)
    ).apply(instance, NegateValue::new));

    @Override
    public float calculate(int level) {
        return -this.input.calculate(level);
    }

    @Override
    public @NotNull MapCodec<? extends LevelBasedValue> codec() {
        return CODEC;
    }
}