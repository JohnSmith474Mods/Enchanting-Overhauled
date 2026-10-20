package johnsmith.enchantingoverhauled.menu.enchant.controller;

import net.minecraft.world.item.enchantment.Enchantment;

/**
 * A functional interface strategy for abstracting the logic that determines the experience level
 * and Lapis Lazuli cost for executing a specific enchantment.
 */
@FunctionalInterface
public interface EnchantCostCalculator {

    // region Methods
    /**
     * Calculates the raw integer cost to apply the given enchantment.
     *
     * @param enchantment The enchantment being evaluated.
     * @return The integer cost.
     */
    int calculateCost(Enchantment enchantment);
    // endregion

    // region Constants
    /**
     * The standard implementation for vanilla-like cost mapping, basing the cost on
     * the enchantment's rarity weight and natural max level.
     */
    EnchantCostCalculator DEFAULT = (enchantment) -> {
        if (enchantment.getMaxLevel() == 1) return 3;
        return enchantment.getWeight() % 3 + 1;
    };
    // endregion
}