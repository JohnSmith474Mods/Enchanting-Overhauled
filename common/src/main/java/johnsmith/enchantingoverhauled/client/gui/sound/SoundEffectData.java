package johnsmith.enchantingoverhauled.client.gui.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

/**
 * A sealed interface following the Command pattern for executing UI sound effects.
 * Handles the varying raw SoundEvent types found in vanilla Minecraft.
 */
public sealed interface SoundEffectData permits
        SoundEffectData.SoundEvent,
        SoundEffectData.HolderReference,
        SoundEffectData.Holder {

    /**
     * Executes the sound effect command, playing it locally for the client.
     */
    void play();

    /**
     * Implementation for direct, unboxed SoundEvent instances.
     */
    record SoundEvent(net.minecraft.sounds.SoundEvent event, float pitch, float volume) implements SoundEffectData {
        @Override
        public void play() {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(this.event, this.pitch, this.volume));
        }
    }

    /**
     * Implementation for Holder.Reference wrapped SoundEvent instances.
     */
    record HolderReference(net.minecraft.core.Holder.Reference<net.minecraft.sounds.SoundEvent> event, float pitch, float volume) implements SoundEffectData {
        @Override
        public void play() {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(this.event.value(), this.pitch, this.volume));
        }
    }

    /**
     * Implementation for generic Holder wrapped SoundEvent instances.
     */
    record Holder(net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> event, float pitch, float volume) implements SoundEffectData {
        @Override
        public void play() {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(this.event.value(), this.pitch, this.volume));
        }
    }
}