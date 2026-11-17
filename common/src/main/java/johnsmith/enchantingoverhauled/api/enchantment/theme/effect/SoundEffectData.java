package johnsmith.enchantingoverhauled.api.enchantment.theme.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

/**
 * A data-driven record defining the audio properties for a sound event associated with an enchantment theme.
 * <p>
 * This record specifies the sound to play and how its volume and pitch should be randomized upon emission.
 * The sound is played locally by the client upon receiving the particle effect command from the server.
 *
 * @param effect          A {@link Holder} referencing the {@link SoundEvent} to play (e.g., "minecraft:particle.soul_escape").
 * @param volume          The base volume (0.0 to 1.0) for the sound.
 * @param volumeVariance  A random offset added to the base volume.
 * @param pitch           The base pitch (0.0 to 2.0) for the sound.
 * @param pitchVariance   A random offset added to the base pitch.
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
     * It uses {@link SoundEvent#CODEC} to correctly handle the sound event holder.
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