package johnsmith.enchantingoverhauled.advancement;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Handles the registration of advancement criteria triggers on the Fabric platform.
 */
public class Advancements {

    /**
     * Registers all custom criteria triggers with the vanilla registry.
     */
    public static void initialize() {
        Registry.register(BuiltInRegistries.TRIGGER_TYPES,
                new ResourceLocation(Constants.MOD_ID, "activate_altar"),
                CriteriaRegistry.ACTIVATE_ALTAR);
    }
}