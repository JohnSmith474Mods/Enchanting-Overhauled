package johnsmith.enchantingoverhauled.api.enchantment.theme.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

/**
 * A data-driven record defining the audio properties for a sound event associated with an
 * {@link johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme}.
 * <p>
 * Specifies the sound to play and how its volume and pitch should be randomized upon emission.
 *
 * @param effect         A {@link Holder} referencing the {@link SoundEvent} to play (e.g., {@code minecraft:particle.soul_escape}).
 * @param volume         The base volume ({@code 0.0} to {@code 1.0}) for the sound.
 * @param volumeVariance A random offset range added to or subtracted from the base volume.
 * @param pitch          The base pitch ({@code 0.0} to {@code 2.0}) for the sound.
 * @param pitchVariance  A random offset range added to or subtracted from the base pitch.
 */
public record SoundEffectData(
        Holder<SoundEvent> effect,
        float volume,
        float volumeVariance,
        float pitch,
        float pitchVariance
) {

    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     * Uses {@link SoundEvent#CODEC} to handle the sound event holder.
     */
    public static final Codec<SoundEffectData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    SoundEvent.CODEC.fieldOf("effect").forGetter(SoundEffectData::effect),
                    Codec.FLOAT.fieldOf("volume").forGetter(SoundEffectData::volume),
                    Codec.FLOAT.fieldOf("volume_variance").forGetter(SoundEffectData::volumeVariance),
                    Codec.FLOAT.fieldOf("pitch").forGetter(SoundEffectData::pitch),
                    Codec.FLOAT.fieldOf("pitch_variance").forGetter(SoundEffectData::pitchVariance)
            ).apply(instance, SoundEffectData::new)
    );
}