package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record BowChargeTimeEffect(LevelBasedValue amount) {
    public static final Codec<BowChargeTimeEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(BowChargeTimeEffect::amount)
    ).apply(instance, BowChargeTimeEffect::new));
}