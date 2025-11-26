package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record BindingCurseEffect(LevelBasedValue chance) {
    public static final Codec<BindingCurseEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("chance").forGetter(BindingCurseEffect::chance)
    ).apply(instance, BindingCurseEffect::new));
}