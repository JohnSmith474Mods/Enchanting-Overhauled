package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record VanishingCurseEffect(LevelBasedValue chance) {
    public static final Codec<VanishingCurseEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("chance").forGetter(VanishingCurseEffect::chance)
    ).apply(instance, VanishingCurseEffect::new));
}