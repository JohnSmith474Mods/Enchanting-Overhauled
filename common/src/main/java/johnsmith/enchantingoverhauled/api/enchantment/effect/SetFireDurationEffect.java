package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import johnsmith.enchantingoverhauled.api.enchantment.accessor.FireDurationAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record SetFireDurationEffect(LevelBasedValue duration) implements EnchantmentEntityEffect {
    public static final MapCodec<SetFireDurationEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(SetFireDurationEffect::duration)
    ).apply(instance, SetFireDurationEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse context, Entity entity, Vec3 origin) {
        if (entity instanceof FireDurationAccessor accessor) {
            // Calculate duration in seconds
            float seconds = this.duration.calculate(enchantmentLevel);
            accessor.enchanting_overhauled$setFireDuration(seconds);
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}