package johnsmith.enchantingoverhauled.api.enchantment.theme.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

/**
 * A data-driven record defining how sounds should play for a theme.
 *
 * @param effect The sound event (e.g., "minecraft:particle.soul_escape").
 * @param volume The base volume.
 * @param volumeVariance The random offset to add to the volume.
 * @param pitch The base pitch.
 * @param pitchVariance The random offset to add to the pitch.
 */
public record SoundEffectData(
        Holder<SoundEvent> effect,
        float volume,
        float volumeVariance,
        float pitch,
        float pitchVariance
) {
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