package johnsmith.api.enchantment.theme.accessor;

import johnsmith.api.enchantment.theme.EnchantmentTheme;
import net.minecraft.registry.RegistryKey;

/**
 * Accessor interface to add EnchantmentTheme support to the base Enchantment class.
 */
public interface EnchantmentThemeAccessor {
    /**
     * Gets the theme associated with this enchantment.
     * @return The EnchantmentTheme.
     */
    RegistryKey<EnchantmentTheme> getTheme();

    /**
     * Sets the theme for this enchantment.
     * @param theme The EnchantmentTheme to set.
     */
    void setTheme(RegistryKey<EnchantmentTheme> theme);
}
