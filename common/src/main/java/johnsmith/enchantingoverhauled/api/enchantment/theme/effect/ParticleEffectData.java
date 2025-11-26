package johnsmith.enchantingoverhauled.api.enchantment.theme.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.phys.Vec3;

/**
 * A data-driven record defining the properties of a theme's particle.
 * Iterations and chance are handled by the parent EffectData.
 *
 * @param effect The particle type (e.g., "minecraft:soul").
 * @param velocity The base velocity to apply to the particle.
 * @param velocityVariance Random variance to add to the base velocity.
 * @param offset The base offset from the spawn position.
 * @param offsetVariance Random variance to add to the base offset.
 */
public record ParticleEffectData(
        ParticleType<?> effect,
        Vec3 velocity,
        Vec3 velocityVariance,
        Vec3 offset,
        Vec3 offsetVariance
) {
    public static final Codec<ParticleEffectData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // Corrected: Use the byNameCodec() from the registry
                    BuiltInRegistries.PARTICLE_TYPE.byNameCodec().fieldOf("effect").forGetter(ParticleEffectData::effect),
                    Vec3.CODEC.fieldOf("velocity").forGetter(ParticleEffectData::velocity),
                    Vec3.CODEC.fieldOf("velocity_variance").forGetter(ParticleEffectData::velocityVariance),
                    Vec3.CODEC.fieldOf("offset").forGetter(ParticleEffectData::offset),
                    Vec3.CODEC.fieldOf("offset_variance").forGetter(ParticleEffectData::offsetVariance)
            ).apply(instance, ParticleEffectData::new)
    );
}