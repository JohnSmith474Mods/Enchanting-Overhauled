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

import net.minecraftforge.registries.DataPackRegistryEvent;

public final class ForgeRegistries {

    public static void initialize(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(EnchantmentThemes.THEME_REGISTRY_KEY, EnchantmentTheme.CODEC, EnchantmentTheme.CODEC);
        event.dataPackRegistry(MagicSchools.REGISTRY_KEY, MagicSchool.CODEC, MagicSchool.CODEC);
        event.dataPackRegistry(EnchantRecipes.RECIPE_REGISTRY_KEY, EnchantRecipe.CODEC, EnchantRecipe.CODEC);
        event.dataPackRegistry(AiSpellFields.REGISTRY_KEY, AiSpellField.CODEC, AiSpellField.CODEC);
        event.dataPackRegistry(LootInjections.REGISTRY_KEY, LootInjection.CODEC, LootInjection.CODEC);
    }

    private ForgeRegistries() {}
}