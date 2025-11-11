package johnsmith.api.enchantment.theme.registry;

import johnsmith.EnchantingOverhauled;
import johnsmith.api.enchantment.theme.EnchantmentTheme;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

/**
 * Handles the registration of all EnchantmentThemes.
 * This class only holds the RegistryKeys. The themes themselves are loaded
 * from JSON files in `data/enchanting_overhauled/enchantment_theme/`.
 */
public class EnchantmentThemeRegistry {

    /**
     * The registry key for our new custom registry.
     */
    public static final RegistryKey<Registry<EnchantmentTheme>> THEME_REGISTRY_KEY =
            RegistryKey.ofRegistry(new Identifier("minecraft", "enchantment_theme"));

    /**
     * RegistryKey for the default theme, which can be loaded from JSON.
     */
    public static final RegistryKey<EnchantmentTheme> DEFAULT =
            RegistryKey.of(THEME_REGISTRY_KEY, new Identifier(EnchantingOverhauled.MOD_ID, "default"));

    /**
     * RegistryKey for the marine theme, which can be loaded from JSON.
     */
    public static final RegistryKey<EnchantmentTheme> MARINE =
            RegistryKey.of(THEME_REGISTRY_KEY, new Identifier(EnchantingOverhauled.MOD_ID, "marine"));

    /**
     * RegistryKey for the nether theme, which can be loaded from JSON.
     * The ID does not include the file extension.
     */
    public static final RegistryKey<EnchantmentTheme> NETHER =
            RegistryKey.of(THEME_REGISTRY_KEY, new Identifier(EnchantingOverhauled.MOD_ID, "nether"));

    /**
     * Called by the main mod initializer to load this class and ensure keys are initialized.
     */
    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing enchantment theme registry...");
        // This method's only purpose is to be called from onInitialize to ensure the
        // static RegistryKey fields above are loaded by the JVM.
    }
}

