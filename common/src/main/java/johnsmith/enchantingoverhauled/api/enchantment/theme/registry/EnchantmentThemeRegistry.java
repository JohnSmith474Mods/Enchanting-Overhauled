package johnsmith.enchantingoverhauled.api.enchantment.theme.registry;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Manages the static {@link ResourceKey} references for the custom, data-driven
 * {@link EnchantmentTheme} registry.
 * <p>
 * This class primarily holds the keys used during data loading and runtime lookups.
 * The theme data itself is loaded dynamically from JSON files within
 * {@code data/enchanting_overhauled/enchantment_theme/}.
 */
public class EnchantmentThemeRegistry {

    /**
     * The {@link ResourceKey} used to identify the custom registry containing all {@link EnchantmentTheme}s.
     * <p>
     * Note: The namespace is set to {@code minecraft} for compatibility with built-in data systems.
     */
    public static final ResourceKey<Registry<EnchantmentTheme>> THEME_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("minecraft", "enchantment_theme"));

    /**
     * The {@link ResourceKey} for the default theme, which is used when no thematic power providers are found.
     */
    public static final ResourceKey<EnchantmentTheme> DEFAULT =
            ResourceKey.create(THEME_REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "default"));

    /**
     * The {@link ResourceKey} for the marine theme.
     */
    public static final ResourceKey<EnchantmentTheme> MARINE =
            ResourceKey.create(THEME_REGISTRY_KEY,ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marine"));

    /**
     * The {@link ResourceKey} for the nether theme.
     */
    public static final ResourceKey<EnchantmentTheme> NETHER =
            ResourceKey.create(THEME_REGISTRY_KEY,ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "nether"));

    /**
     * Initializes the registry class.
     * <p>
     * This method is called early in the mod initialization process specifically to force
     * the JVM to load this class and ensure all static {@link ResourceKey} fields are initialized.
     */
    public static void initialize() {
        Constants.LOG.info("Initializing enchantment theme registry...");
    }
}