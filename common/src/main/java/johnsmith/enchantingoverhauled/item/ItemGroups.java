package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Central handler for Creative Mode Tabs (Item Groups) in the common module.
 * <p>
 * Defines the key for the custom "Enchanting Overhauled" tab and provides methods
 * to populate it and other vanilla tabs with the mod's items.
 */
public class ItemGroups {

    /**
     * The RegistryKey for the mod's custom creative tab.
     */
    public static final ResourceKey<CreativeModeTab> ENCHANTING_GROUP_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanting_group")
    );

    /**
     * Populates the custom "Enchanting Overhauled" creative tab.
     */
    public static void displayItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        // 1. Add the tables
        output.accept(Services.PLATFORM.getDeactivatedEnchantingTable());
        output.accept(Services.PLATFORM.getDisturbedEnchantingTable());

        // 2. Add all enchantments with custom levels
        generateTomes(parameters, output);
    }

    /**
     * Populates the vanilla "Ingredients" tab with Enchanted Tomes.
     */
    public static void populateIngredients(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        generateTomes(params, output);
    }

    /**
     * Populates the vanilla "Functional Blocks" tab with the mod's tables.
     */
    public static void populateFunctional(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        output.accept(Services.PLATFORM.getDeactivatedEnchantingTableItem());
        output.accept(Services.PLATFORM.getDisturbedEnchantingTableItem());
    }

    /**
     * Helper method to iterate all enchantments and generate "Over-Leveled" Tomes.
     * <p>
     * In 1.21, we iterate the dynamic registry from the display parameters.
     * We do not need to check feature flags explicitly; the registry provided
     * by the parameters only contains enabled enchantments.
     */
    private static void generateTomes(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        // 1. Retrieve the dynamic registry
        HolderLookup.RegistryLookup<Enchantment> registry = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);

        // 2. Iterate through all registered enchantments (Holders)
        // The registry.listElements() stream is already filtered for enabled features by the game.
        registry.listElements().forEach(holder -> {
            Enchantment enchantment = holder.value();

            // 3. Apply your "Over-Level" logic
            int maxLevel = enchantment.getMaxLevel();
            // Force single-level enchants (like Mending) to stay at 1, otherwise boost by 1.
            int level = (maxLevel == 1) ? 1 : maxLevel + 1;

            ItemStack stack = new ItemStack(Services.PLATFORM.getEnchantedTome());

            // 4. Set the stored enchantments component using the Holder
            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            builder.set(holder, level);

            stack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());

            output.accept(stack);
        });
    }
}