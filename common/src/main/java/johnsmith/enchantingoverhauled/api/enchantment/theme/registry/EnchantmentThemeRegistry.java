package johnsmith.enchantingoverhauled.api.enchantment.theme.registry;

import johnsmith.enchantingoverhauled.Constants; // Import common Constants
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Handles the registration of all EnchantmentThemes.
 * This class only holds the RegistryKeys. The themes themselves are loaded
 * from JSON files in `data/enchanting_overhauled/enchantment_theme/`.
 */
public class EnchantmentThemeRegistry {

    /**
     * The registry key for our new custom registry.
     */
    public static final ResourceKey<Registry<EnchantmentTheme>> THEME_REGISTRY_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "enchantment_theme"));

    /**
     * RegistryKey for the default theme, which can be loaded from JSON.
     */
    public static final ResourceKey<EnchantmentTheme> DEFAULT =
            ResourceKey.create(THEME_REGISTRY_KEY, new ResourceLocation(Constants.MOD_ID, "default"));

    /**
     * RegistryKey for the marine theme, which can be loaded from JSON.
     */
    public static final ResourceKey<EnchantmentTheme> MARINE =
            ResourceKey.create(THEME_REGISTRY_KEY, new ResourceLocation(Constants.MOD_ID, "marine"));

    /**
     * RegistryKey for the nether theme, which can be loaded from JSON.
     * The ID does not include the file extension.
     */
    public static final ResourceKey<EnchantmentTheme> NETHER =
            ResourceKey.create(THEME_REGISTRY_KEY, new ResourceLocation(Constants.MOD_ID, "nether"));

    /**
     * Called by the main mod initializer to load this class and ensure keys are initialized.
     */
    public static void initialize() {
        Constants.LOG.info("Initializing enchantment theme registry...");
        // This method's only purpose is to be called from onInitialize to ensure the
        // static RegistryKey fields above are loaded by the JVM.
    }
}