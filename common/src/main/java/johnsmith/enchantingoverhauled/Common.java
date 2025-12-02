package johnsmith.enchantingoverhauled;

import johnsmith.enchantingoverhauled.advancement.CriteriaRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.damagesource.DamageTypeRegistry;
import net.minecraft.resources.ResourceLocation;

/**
 * The common entry point for Enchanting Overhauled.
 * <p>
 * This class handles the initialization of features that are shared across all platforms
 * and do not require loader-specific registration mechanisms (like DeferredRegisters)
 * at this stage. It ensures that static registry keys and definitions are loaded into the JVM.
 */
public class Common {

    /**
     * Initializes common mod components.
     * <p>
     * This method must be called by the platform-specific
     * entry points during their initialization phase.
     */
    public static void initialize() {
        Config.initialize();

        EnchantmentThemeRegistry.initialize();

        DamageTypeRegistry.initialize();

        CriteriaRegistry.initialize();
    }

    public static ResourceLocation resource(final String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}