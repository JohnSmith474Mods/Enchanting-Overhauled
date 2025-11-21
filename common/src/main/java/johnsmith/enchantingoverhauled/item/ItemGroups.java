package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.platform.Services;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
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
            new ResourceLocation(Constants.MOD_ID, "enchanting_group")
    );

    /**
     * Populates the custom "Enchanting Overhauled" creative tab.
     * <p>
     * Adds the custom tables and a comprehensive list of Enchanted Tomes,
     * covering all enabled enchantments at levels higher than their vanilla maximums.
     *
     * @param parameters Display parameters (enabled features, permissions).
     * @param output     The output acceptor to add items to.
     */
    public static void displayItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        // 1. Add the tables
        output.accept(Services.PLATFORM.getDeactivatedEnchantingTable());
        output.accept(Services.PLATFORM.getDisturbedEnchantingTable());

        // 2. Add all enchantments with custom levels
        BuiltInRegistries.ENCHANTMENT.stream().forEach(enchantment -> {
            // Check if valid for current features
            if (enchantment.isEnabled(parameters.enabledFeatures())) {
                int maxLevel = enchantment.getMaxLevel();
                int level = (maxLevel == 1) ? 1 : maxLevel + 1;

                ItemStack stack = new ItemStack(Services.PLATFORM.getEnchantedTome());

                // Set the stored enchantments component
                ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                builder.set(enchantment, level);

                stack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());

                output.accept(stack);
            }
        });
    }

    /**
     * Populates the vanilla "Ingredients" tab with Enchanted Tomes.
     *
     * @param params Display parameters.
     * @param output The output acceptor.
     */
    public static void populateIngredients(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        BuiltInRegistries.ENCHANTMENT.stream().forEach(enchantment -> {
            if (enchantment.isEnabled(params.enabledFeatures())) {
                int maxLevel = enchantment.getMaxLevel();
                int level = (maxLevel == 1) ? 1 : maxLevel + 1;

                ItemStack stack = new ItemStack(Services.PLATFORM.getEnchantedTome());

                ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                builder.set(enchantment, level);

                stack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
                output.accept(stack);
            }
        });
    }

    /**
     * Populates the vanilla "Functional Blocks" tab with the mod's tables.
     *
     * @param params Display parameters.
     * @param output The output acceptor.
     */
    public static void populateFunctional(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        output.accept(Services.PLATFORM.getDeactivatedEnchantingTableItem());
        output.accept(Services.PLATFORM.getDisturbedEnchantingTableItem());
    }
}