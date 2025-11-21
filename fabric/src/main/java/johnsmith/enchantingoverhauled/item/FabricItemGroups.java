package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Constants;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

/**
 * Registration handler for Creative Mode Tabs on the Fabric platform.
 * <p>
 * Uses the Fabric API to register the custom tab and modify vanilla tabs.
 */
public class FabricItemGroups {

    /**
     * Initializes item groups and tab events.
     */
    public static void initialize() {
        // Register the custom group
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                ItemGroups.ENCHANTING_GROUP_KEY,
                FabricItemGroup.builder()
                        .title(Component.translatable("itemGroup.enchanting_overhauled.enchanting_group"))
                        .icon(() -> new ItemStack(Items.ENCHANTED_TOME))
                        .displayItems(ItemGroups::displayItems) // Call common logic
                        .build()
        );

        // Add items to the Ingredients tab
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(
                (entries) -> ItemGroups.populateIngredients(entries.getContext(), entries)
        );

        // Add items to the Functional Blocks tab and remove the vanilla enchanting table
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(
                (entries) -> {
                    ItemGroups.populateFunctional(entries.getContext(), entries);
                    if (entries instanceof FabricItemGroupEntries fabricEntries) {
                        fabricEntries.getDisplayStacks().removeIf(stack -> stack.is(net.minecraft.world.item.Items.ENCHANTING_TABLE));
                        fabricEntries.getSearchTabStacks().removeIf(stack -> stack.is(net.minecraft.world.item.Items.ENCHANTING_TABLE));
                    }
                }
        );
    }
}