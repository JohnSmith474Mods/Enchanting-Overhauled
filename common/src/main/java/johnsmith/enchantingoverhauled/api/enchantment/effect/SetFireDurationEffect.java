package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import johnsmith.enchantingoverhauled.api.enchantment.accessor.FireDurationAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

/**
 * An {@link EnchantmentEntityEffect} that is applied to a projectile entity
 * (such as an arrow) to set a custom, level-based fire duration (in seconds)
 * when that projectile hits a mob.
 * <p>
 * This effect relies on the projectile entity implementing the
 * {@link FireDurationAccessor} interface to store the desired fire duration,
 * which is later read by the projectile's hit logic.
 *
 * @param duration A {@link LevelBasedValue} that calculates the total time (in seconds)
 * for which the target entity should burn at a specific enchantment level.
 */
public record SetFireDurationEffect(LevelBasedValue duration) implements EnchantmentEntityEffect {
    /**
     * The codec responsible for serializing and deserializing instances of this record from data files (e.g., JSON).
     */
    public static final MapCodec<SetFireDurationEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(SetFireDurationEffect::duration)
    ).apply(instance, SetFireDurationEffect::new));

    /**
     * Applies the effect by calculating the fire duration based on the enchantment level
     * and setting it onto the projectile entity (if it is a {@code FireDurationAccessor}).
     * The actual ignition is deferred to the projectile's hit logic.
     *
     * @param level            The server level.
     * @param enchantmentLevel The level of the enchantment causing the effect.
     * @param context          The context of the enchantment use (e.g., the item owner).
     * @param entity           The entity carrying the projectile or the projectile itself (expected to be the projectile).
     * @param origin           The position where the enchantment event originated.
     */
    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse context, Entity entity, Vec3 origin) {
        if (entity instanceof FireDurationAccessor accessor) {
            float seconds = this.duration.calculate(enchantmentLevel);
            accessor.enchanting_overhauled$setFireDuration(seconds);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@link MapCodec} for this effect type.
     */
    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}