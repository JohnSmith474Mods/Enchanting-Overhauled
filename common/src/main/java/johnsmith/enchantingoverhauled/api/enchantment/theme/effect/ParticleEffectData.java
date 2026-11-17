package johnsmith.enchantingoverhauled.api.enchantment.theme.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;

/**
 * A data-driven record defining the specific parameters for spawning a particle effect
 * associated with an enchantment theme.
 * <p>
 * The spawn rate (iterations and chance) is controlled by the parent {@link EffectData} record.
 *
 * @param effect           The particle type to spawn (e.g., "minecraft:soul").
 * @param velocity         The base directional velocity (x, y, z) to apply to the spawned particle.
 * @param velocityVariance A random variance vector (x, y, z) added to the base velocity for scattering.
 * @param offset           The base offset vector (x, y, z) from the spawn position (center of table/provider).
 * @param offsetVariance   A random variance vector (x, y, z) added to the base offset for scattering the spawn point.
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
     * It uses {@link BuiltInRegistries#PARTICLE_TYPE}'s codec for resolving the {@code ParticleType}.
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