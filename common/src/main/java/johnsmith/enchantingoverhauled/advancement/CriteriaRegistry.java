package johnsmith.enchantingoverhauled.advancement;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Registry handler for custom advancement criteria triggers.
 * <p>
 * Holds the singleton instances of custom triggers used by the mod.
 */
public class CriteriaRegistry {

    /**
     * The singleton instance of the Activate Altar trigger.
     */
    public static final ActivateAltarTrigger ACTIVATE_ALTAR = new ActivateAltarTrigger();


    /**
     * Initializes the registry class.
     * <p>
     * Calling this method ensures that the static fields are loaded and initialized
     * by the JVM before they are referenced by platform-specific registration code.
     */
    public static void initialize() {
        Constants.LOG.info("Initializing criteria registry...");
    }
}