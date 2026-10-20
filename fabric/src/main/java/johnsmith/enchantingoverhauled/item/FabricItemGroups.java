package johnsmith.enchantingoverhauled.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

public class FabricItemGroups {
    public static void initialize() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                ItemGroups.ENCHANTING_GROUP_KEY,
                FabricItemGroup.builder()
                        .title(Component.translatable("itemGroup.enchanting_overhauled.enchanting_group"))
                        .icon(() -> new ItemStack(Items.ENCHANTED_TOME.get()))
                        .displayItems(ItemGroups::displayItems)
                        .build()
        );

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(
                (entries) -> ItemGroups.populateIngredients(entries.getContext(), entries)
        );

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(
                ItemGroups::populateSpawnEggs
        );

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(
                (entries) -> {
                    ItemGroups.populateFunctional(entries);
                    // disable the enchanting_table removal
                    if (false && entries instanceof FabricItemGroupEntries fabricEntries) {
                        fabricEntries.getDisplayStacks().removeIf(stack -> stack.is(net.minecraft.world.item.Items.ENCHANTING_TABLE));
                        fabricEntries.getSearchTabStacks().removeIf(stack -> stack.is(net.minecraft.world.item.Items.ENCHANTING_TABLE));
                    }
                }
        );
    }
}