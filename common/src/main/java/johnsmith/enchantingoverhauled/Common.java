package johnsmith.enchantingoverhauled;

import johnsmith.enchantingoverhauled.api.damagesource.DamageTypes;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipes;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellFields;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;

/**
 * The common entry point for Enchanting Overhauled.
 * <p>
 * Only initializes static {@link net.minecraft.resources.ResourceKey} definitions that do not
 * mutate frozen vanilla {@link net.minecraft.core.registries.BuiltInRegistries}, ensuring compatibility
 * with NeoForge and Forge's strict {@code RegisterEvent} lifecycle.
 */
public final class Common {
    public static void initialize() {
        EnchantmentThemes.initialize();
        MagicSchools.initialize();
        EnchantRecipes.initialize();
        AiSpellFields.initialize();

        DamageTypes.initialize();
    }

    private Common() {}
}