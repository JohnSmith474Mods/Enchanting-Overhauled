package johnsmith.api.enchantment.theme.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * A wrapper record that holds all effect data (particles and sound)
 * for a specific theme, as defined in the JSON.
 *
 * @param iterations The number of times to try spawning an effect per tick.
 * @param chance The chance (as in "1 in X") for each iteration to succeed.
 * @param particle The (optional) particle data.
 * @param sound The (optional) sound data.
 */
public record EffectData(
        int iterations,
        int chance,
        Optional<ParticleEffectData> particle,
        Optional<SoundEffectData> sound
) {
    public static final Codec<EffectData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("iterations").forGetter(EffectData::iterations),
                    Codec.INT.fieldOf("chance").forGetter(EffectData::chance),
                    ParticleEffectData.CODEC.optionalFieldOf("particle").forGetter(EffectData::particle),
                    SoundEffectData.CODEC.optionalFieldOf("sound").forGetter(EffectData::sound)
            ).apply(instance, EffectData::new)
    );
}
