package johnsmith.enchantingoverhauled.api.enchantment.theme.accessor;

import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import net.minecraft.resources.ResourceKey;

/**
 * Accessor interface to add EnchantmentTheme support to the base Enchantment class.
 */
public interface EnchantmentThemeAccessor {
    /**
     * Gets the theme associated with this enchantment.
     * @return The EnchantmentTheme.
     */
    ResourceKey<EnchantmentTheme> enchanting_overhauled$getTheme();

    /**
     * Sets the theme for this enchantment.
     * @param theme The EnchantmentTheme to set.
     */
    void enchanting_overhauled$setTheme(ResourceKey<EnchantmentTheme> theme);
}
