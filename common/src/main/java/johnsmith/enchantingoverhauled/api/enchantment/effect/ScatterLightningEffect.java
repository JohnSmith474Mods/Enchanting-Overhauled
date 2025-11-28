package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import johnsmith.enchantingoverhauled.util.WorldScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

/**
 * An {@link EnchantmentEntityEffect} that summons multiple lightning bolts on and around a target entity.
 * <p>
 * This effect supports temporal scattering of bolts using a configurable {@code interval}
 * and physical scattering using a configurable {@code radius}. It relies on a custom
 * {@link WorldScheduler} mixin on {@link ServerLevel} for delayed execution.
 *
 * @param count    A {@link LevelBasedValue} calculating the number of lightning bolts to summon.
 * @param radius   The maximum horizontal distance (in blocks) from the target's center position
 * where scattered bolts will strike.
 * @param interval The delay (in ticks) between the summoning of successive lightning bolts.
 */
public record ScatterLightningEffect(LevelBasedValue count, float radius, int interval) implements EnchantmentEntityEffect {

    /**
     * The codec responsible for serializing and deserializing instances of this record from data files (e.g., JSON).
     * It maps the {@code "count"} (required), {@code "radius"} (required), and {@code "interval"} (optional, defaults to 0) fields.
     */
    public static final MapCodec<ScatterLightningEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("count").forGetter(ScatterLightningEffect::count),
            Codec.FLOAT.fieldOf("radius").forGetter(ScatterLightningEffect::radius),
            Codec.INT.optionalFieldOf("interval", 0).forGetter(ScatterLightningEffect::interval)
    ).apply(instance, ScatterLightningEffect::new));

    /**
     * Applies the lightning scattering effect to the target entity.
     * <p>
     * Summons the calculated number of lightning bolts. The first bolt strikes the target's center.
     * Subsequent bolts are randomly scattered within the defined radius and scheduled to strike
     * at intervals if {@code interval > 0}.
     *
     * @param level            The server level where the effect is applied.
     * @param enchantmentLevel The level of the enchantment causing the effect.
     * @param context          The context of the enchantment use (e.g., the item owner).
     * @param target           The entity being hit/affected by the enchantment.
     * @param origin           The position where the enchantment event originated (unused in this logic).
     */
    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse context, Entity target, Vec3 origin) {
        int boltCount = (int) this.count.calculate(enchantmentLevel);
        Entity owner = context.owner();
        ServerPlayer playerCause = (owner instanceof ServerPlayer sp) ? sp : null;
        Vec3 center = target.position();

        for (int i = 0; i < boltCount; i++) {
            final int index = i;
            Runnable spawnTask = () -> {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.EVENT);
                if (bolt != null) {
                    if (index == 0) {
                        // Primary bolt hits target directly
                        bolt.setPos(center);
                    } else {
                        // Scattered bolts
                        double offsetX = (level.random.nextDouble() - 0.5D) * 2.0D * this.radius;
                        double offsetZ = (level.random.nextDouble() - 0.5D) * 2.0D * this.radius;
                        bolt.setPos(center.x + offsetX, center.y, center.z + offsetZ);
                    }
                    bolt.setCause(playerCause);
                    level.addFreshEntity(bolt);
                }
            };

            if (i == 0 || this.interval <= 0) {
                spawnTask.run();
            } else {
                ((WorldScheduler) level).enchanting_overhauled$schedule(i * this.interval, spawnTask);
            }
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