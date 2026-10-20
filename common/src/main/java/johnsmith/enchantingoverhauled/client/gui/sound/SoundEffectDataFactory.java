package johnsmith.enchantingoverhauled.client.gui.sound;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

/**
 * Factory utility to automatically wrap vanilla's mixed SoundEvent types into
 * unified SoundEffectData command objects.
 */
public class SoundEffectDataFactory {

    /**
     * Wraps a raw, unboxed SoundEvent.
     */
    public static SoundEffectData create(SoundEvent event, float pitch, float volume) {
        return new SoundEffectData.SoundEvent(event, pitch, volume);
    }

    /**
     * Wraps a Holder.Reference boxed SoundEvent.
     */
    public static SoundEffectData create(Holder.Reference<SoundEvent> event, float pitch, float volume) {
        return new SoundEffectData.HolderReference(event, pitch, volume);
    }

    /**
     * Wraps a generic Holder boxed SoundEvent.
     */
    public static SoundEffectData create(Holder<SoundEvent> event, float pitch, float volume) {
        return new SoundEffectData.Holder(event, pitch, volume);
    }
}