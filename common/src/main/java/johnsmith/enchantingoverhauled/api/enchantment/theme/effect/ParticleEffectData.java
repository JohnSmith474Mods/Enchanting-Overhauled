package johnsmith.enchantingoverhauled.api.enchantment.theme.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;

/**
 * A data-driven record defining the specific parameters for spawning a particle effect
 * associated with an {@link johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme}.
 * <p>
 * The spawn rate (iterations and chance) is controlled by the parent {@link EffectData} record.
 *
 * @param effect           The {@link ParticleType} to spawn (e.g., {@code minecraft:soul}).
 * @param velocity         The base directional velocity vector {@code (x, y, z)} applied to the spawned particle.
 * @param velocityVariance A random variance vector {@code (x, y, z)} added to the base velocity for scattering.
 * @param offset           The base offset vector {@code (x, y, z)} from the center of the table, provider, or entity.
 * @param offsetVariance   A random variance vector {@code (x, y, z)} added to the base offset for scattering the spawn point.
 */
public record ParticleEffectData(
        ParticleType<?> effect,
        Vec3 velocity,
        Vec3 velocityVariance,
        Vec3 offset,
        Vec3 offsetVariance
) {

    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     * Uses {@link BuiltInRegistries#PARTICLE_TYPE}'s name codec to resolve the {@link ParticleType}.
     */
    public static final Codec<ParticleEffectData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.PARTICLE_TYPE.byNameCodec().fieldOf("effect").forGetter(ParticleEffectData::effect),
                    Vec3.CODEC.fieldOf("velocity").forGetter(ParticleEffectData::velocity),
                    Vec3.CODEC.fieldOf("velocity_variance").forGetter(ParticleEffectData::velocityVariance),
                    Vec3.CODEC.fieldOf("offset").forGetter(ParticleEffectData::offset),
                    Vec3.CODEC.fieldOf("offset_variance").forGetter(ParticleEffectData::offsetVariance)
            ).apply(instance, ParticleEffectData::new)
    );
}