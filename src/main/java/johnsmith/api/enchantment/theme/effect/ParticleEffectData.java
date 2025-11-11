package johnsmith.api.enchantment.theme.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Vec3d;

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
        Vec3d velocity,
        Vec3d velocityVariance,
        Vec3d offset,
        Vec3d offsetVariance
) {
    public static final Codec<ParticleEffectData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Registries.PARTICLE_TYPE.getCodec().fieldOf("effect").forGetter(ParticleEffectData::effect),
                    Vec3d.CODEC.fieldOf("velocity").forGetter(ParticleEffectData::velocity),
                    Vec3d.CODEC.fieldOf("velocity_variance").forGetter(ParticleEffectData::velocityVariance),
                    Vec3d.CODEC.fieldOf("offset").forGetter(ParticleEffectData::offset),
                    Vec3d.CODEC.fieldOf("offset_variance").forGetter(ParticleEffectData::offsetVariance)
            ).apply(instance, ParticleEffectData::new)
    );
}