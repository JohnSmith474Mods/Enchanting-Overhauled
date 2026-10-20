package johnsmith.enchantingoverhauled.api.enchantment.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * A data-driven record defining an enchanting transmutation recipe.
 * <p>
 * Determines how an input item (or tag of items) transforms into an output item when an enchantment
 * is applied to it in the Enchanting Altar, Anvil, Tome Bartering, or loot generation (for example,
 * transforming Paper into an Enchanted Page or a Book into an Enchanted Book), as well as how
 * Grindstones and catalyst consumption revert the output item back to its input item.
 *
 * @param input          A {@link HolderSet} of eligible input {@link Item}s that match this recipe.
 * @param output         The resulting output {@link Item} holder produced when enchanted.
 * @param attachment     The {@link EnchantmentAttachment} mode dictating where the enchantment is stored on the output.
 * @param enchantability The base enchantability value assigned to the input item during altar generation (minimum {@code 1}).
 * @see EnchantRecipes
 */
public record EnchantRecipe(
        HolderSet<Item> input,
        Holder<Item> output,
        EnchantmentAttachment attachment,
        int enchantability
) {

    /**
     * The codec responsible for serializing and deserializing {@link EnchantRecipe} definitions from JSON.
     */
    public static final Codec<EnchantRecipe> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("input").forGetter(EnchantRecipe::input),
                    BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("output").forGetter(EnchantRecipe::output),
                    EnchantmentAttachment.CODEC.fieldOf("attachment").forGetter(EnchantRecipe::attachment),
                    Codec.INT.optionalFieldOf("enchantability", 1).forGetter(EnchantRecipe::enchantability)
            ).apply(instance, EnchantRecipe::new)
    ));

    /**
     * Returns the effective enchantability for this recipe, clamped to at least {@code 1}.
     *
     * @return The clamped integer enchantability value.
     */
    @Override
    public int enchantability() {
        return Math.clamp(enchantability, 1, Integer.MAX_VALUE);
    }

    /**
     * Searches the dynamic {@link EnchantRecipes#RECIPE_REGISTRY_KEY} registry to find a recipe
     * whose {@link #input()} matches the given target {@link ItemStack} (e.g., {@code Book -> Enchanted Book}).
     *
     * @param registryAccess The active {@link RegistryAccess} providing the recipe registry.
     * @param target         The input {@link ItemStack} to match.
     * @return An {@link Optional} containing the matching {@link EnchantRecipe}, or empty if none matches.
     */
    public static Optional<EnchantRecipe> get(RegistryAccess registryAccess, ItemStack target) {
        return registryAccess.registry(EnchantRecipes.RECIPE_REGISTRY_KEY).flatMap(registry -> {
            for (EnchantRecipe recipe : registry) {
                if (recipe.input().contains(target.getItemHolder())) {
                    return Optional.of(recipe);
                }
            }
            return Optional.empty();
        });
    }

    /**
     * Searches the dynamic {@link EnchantRecipes#RECIPE_REGISTRY_KEY} registry in reverse to find a recipe
     * whose {@link #output()} matches the given {@link ItemStack} (e.g., reverting an {@code Enchanted Page -> Paper}).
     *
     * @param registryAccess The active {@link RegistryAccess} providing the recipe registry.
     * @param output         The output {@link ItemStack} to reverse-match.
     * @return An {@link Optional} containing the matching {@link EnchantRecipe}, or empty if none matches.
     */
    public static Optional<EnchantRecipe> reverse(RegistryAccess registryAccess, ItemStack output) {
        return registryAccess.registry(EnchantRecipes.RECIPE_REGISTRY_KEY).flatMap(registry -> {
            for (EnchantRecipe recipe : registry) {
                if (recipe.output().value() == output.getItem()) {
                    return Optional.of(recipe);
                }
            }
            return Optional.empty();
        });
    }
}