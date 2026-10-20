package johnsmith.enchantingoverhauled.api.enchantment.recipe;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Manages the static {@link ResourceKey} reference for the custom data-driven
 * {@link EnchantRecipe} registry.
 */
public class EnchantRecipes {

    /**
     * The {@link ResourceKey} used to identify the dynamic registry containing all {@link EnchantRecipe} definitions.
     */
    public static final ResourceKey<Registry<EnchantRecipe>> RECIPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("enchant_recipe"));

    /**
     * Triggers static class initialization to ensure the registry key is created.
     */
    public static void initialize() {}
}