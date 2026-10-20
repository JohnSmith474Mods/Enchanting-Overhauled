package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.block.Blocks;

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
        output.accept(Blocks.DEACTIVATED_ENCHANTING_TABLE.get());
        output.accept(Blocks.DISTURBED_ENCHANTING_TABLE.get());

        // 2. Add all enchantments with custom levels
        generateTomes(parameters, output);
        generatePages(parameters, output);
    }

    /**
     * Populates the vanilla "Ingredients" tab with Enchanted Tomes.
     */
    public static void populateIngredients(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        generateTomes(parameters, output);
        generatePages(parameters, output);
    }

    /**
     * Populates the vanilla "Functional Blocks" tab with the mod's tables.
     */
    public static void populateFunctional(CreativeModeTab.Output output) {
        output.accept(Items.DEACTIVATED_ENCHANTING_ALTAR.get());
        output.accept(Items.DISTURBED_ENCHANTING_ALTAR.get());
    }

    public static void populateSpawnEggs(CreativeModeTab.Output output) {
        output.accept(Items.ENCHANTED_TOME_SPAWN_EGG.get());
        output.accept(Items.ENCHANTED_BOOK_SPAWN_EGG.get());
    }

    /**
     * Helper method to iterate all enchantments and generate "Over-Leveled" Tomes.
     * <p>
     * In 1.21, we iterate the dynamic registry from the display parameters.
     * We do not need to check feature flags explicitly; the registry provided
     * by the parameters only contains enabled enchantments.
     */
    private static void generateTomes(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        HolderLookup.RegistryLookup<Enchantment> registry = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);

        registry.listElements().forEach(holder -> {
            Enchantment enchantment = holder.value();

            int maxLevel = enchantment.getMaxLevel();
            int level = (maxLevel == 1) ? 1 : Config.CAN_TOMES_HAVE_GREATER_ENCHANTMENTS.get() ? maxLevel + 1 : maxLevel;

            ItemStack stack = new ItemStack(Items.ENCHANTED_TOME.get());

            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            builder.set(holder, level);

            stack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());

            output.accept(stack);
        });
    }

    private static void generatePages(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        HolderLookup.RegistryLookup<Enchantment> registry = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);

        registry.listElements().forEach(holder -> {

            ItemStack stack = new ItemStack(Items.ENCHANTED_PAGE.get());

            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            builder.set(holder, 1);

            stack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());

            output.accept(stack);
        });
    }
}