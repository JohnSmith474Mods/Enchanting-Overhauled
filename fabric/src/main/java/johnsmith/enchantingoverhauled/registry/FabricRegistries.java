package johnsmith.enchantingoverhauled.registry;

import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipe;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipes;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellField;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellFields;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.api.loot.LootInjection;
import johnsmith.enchantingoverhauled.api.loot.LootInjections;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class FabricRegistries {
    public static void initialize() {
        DynamicRegistries.registerSynced(EnchantmentThemes.THEME_REGISTRY_KEY, EnchantmentTheme.CODEC);
        DynamicRegistries.registerSynced(MagicSchools.REGISTRY_KEY, MagicSchool.CODEC);
        DynamicRegistries.registerSynced(EnchantRecipes.RECIPE_REGISTRY_KEY, EnchantRecipe.CODEC);
        DynamicRegistries.registerSynced(AiSpellFields.REGISTRY_KEY, AiSpellField.CODEC);
        DynamicRegistries.registerSynced(LootInjections.REGISTRY_KEY, LootInjection.CODEC);
    }

    private FabricRegistries() {}
}
