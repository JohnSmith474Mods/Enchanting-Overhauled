package johnsmith.enchantingoverhauled.client.gui.sound.scribe;

import johnsmith.enchantingoverhauled.client.gui.sound.SoundEffectData;
import johnsmith.enchantingoverhauled.client.gui.sound.SoundEffectDataFactory;
import net.minecraft.sounds.SoundEvents;

/**
 * A specification defining the sound effects mapped to interactions within the Scribe minigame.
 */
public record ScribeSoundEffectSpecification(
        SoundEffectData actionIdle,
        SoundEffectData actionStart,
        SoundEffectData actionAbort,
        SoundEffectData actionWon,
        SoundEffectData actionLost,
        SoundEffectData gridReveal,
        SoundEffectData gridFlag,
        SoundEffectData gridUnflag,
        SoundEffectData gameWon,
        SoundEffectData gameLost
) {
    /**
     * The standard default sound mappings for the Scribe interface, resolved via the factory.
     */
    public static final ScribeSoundEffectSpecification DEFAULT = new ScribeSoundEffectSpecification(
            SoundEffectDataFactory.create(SoundEvents.ITEM_PICKUP, 1.0F, 0.5F),           // actionIdle (Take Book / Tome)
            SoundEffectDataFactory.create(SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.5F),        // actionStart (Scribe Enchantment)
            SoundEffectDataFactory.create(SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.25F),       // actionAbort (Abort Scribing)
            SoundEffectDataFactory.create(SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.5F),        // actionWon (Return to Index)
            SoundEffectDataFactory.create(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.5F),       // actionLost (Accept Fate)
            SoundEffectDataFactory.create(SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.5F),        // gridReveal (Opening a field)
            SoundEffectDataFactory.create(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 0.5F), // gridFlag (Flagging a field)
            SoundEffectDataFactory.create(SoundEvents.ENCHANTMENT_TABLE_USE, 0.8F, 0.4F), // gridUnflag (Lower pitch/volume)
            SoundEffectDataFactory.create(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.5F),        // gameWon (Winning the game)
            SoundEffectDataFactory.create(SoundEvents.WITHER_SPAWN, 1.0F, 0.5F)           // gameLost (Losing the game)
    );
}