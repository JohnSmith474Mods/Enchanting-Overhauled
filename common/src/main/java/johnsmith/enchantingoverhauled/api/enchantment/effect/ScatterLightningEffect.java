package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import johnsmith.enchantingoverhauled.util.WorldScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record ScatterLightningEffect(LevelBasedValue count, float radius, int interval) implements EnchantmentEntityEffect {

    public static final MapCodec<ScatterLightningEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("count").forGetter(ScatterLightningEffect::count),
            Codec.FLOAT.fieldOf("radius").forGetter(ScatterLightningEffect::radius),
            Codec.INT.optionalFieldOf("interval", 0).forGetter(ScatterLightningEffect::interval)
    ).apply(instance, ScatterLightningEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse context, Entity target, Vec3 origin) {
        int boltCount = (int) this.count.calculate(enchantmentLevel);
        Entity owner = context.owner();
        ServerPlayer playerCause = (owner instanceof ServerPlayer sp) ? sp : null;
        Vec3 center = target.position();

        for (int i = 0; i < boltCount; i++) {
            final int index = i;
            Runnable spawnTask = () -> {
                // Re-verify entity validity if needed, or just strike at the position
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    if (index == 0) {
                        // Primary bolt hits target directly
                        bolt.moveTo(center);
                    } else {
                        // Scattered bolts
                        double offsetX = (level.random.nextDouble() - 0.5D) * 2.0D * this.radius;
                        double offsetZ = (level.random.nextDouble() - 0.5D) * 2.0D * this.radius;
                        bolt.moveTo(center.x + offsetX, center.y, center.z + offsetZ);
                    }
                    bolt.setCause(playerCause);
                    level.addFreshEntity(bolt);
                }
            };

            // Schedule: First bolt (i=0) is instant. Others are delayed by i * interval.
            if (i == 0 || this.interval <= 0) {
                spawnTask.run();
            } else {
                ((WorldScheduler) level).enchanting_overhauled$schedule(i * this.interval, spawnTask);
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}