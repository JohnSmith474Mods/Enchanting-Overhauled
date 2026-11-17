package johnsmith.enchantingoverhauled.api.enchantment.theme.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * A data-driven record serving as a wrapper to define the visual and auditory effects
 * associated with an {@link johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme}.
 * <p>
 * It controls the spawn rate and properties of both particles and sounds emitted by
 * an active enchanting table or its power providers.
 *
 * @param iterations The number of times the game should attempt to spawn an effect
 * per tick for an active theme.
 * @param chance     The inverse probability of an effect successfully spawning on each attempt,
 * expressed as "1 in X" (e.g., a value of 40 means a 1/40 chance).
 * @param particle   An {@link Optional} containing the specific parameters for particle spawning.
 * @param sound      An {@link Optional} containing the specific parameters for sound playback.
 */
public record EffectData(
        int iterations,
        int chance,
        Optional<ParticleEffectData> particle,
        Optional<SoundEffectData> sound
) {
    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     * It maps all four fields, with {@code particle} and {@code sound} being optional.
     */
    public static final Codec<EffectData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("iterations").forGetter(EffectData::iterations),
                    Codec.INT.fieldOf("chance").forGetter(EffectData::chance),
                    ParticleEffectData.CODEC.optionalFieldOf("particle").forGetter(EffectData::particle),
                    SoundEffectData.CODEC.optionalFieldOf("sound").forGetter(EffectData::sound)
            ).apply(instance, EffectData::new)
    );
}