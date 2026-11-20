package johnsmith.enchantingoverhauled.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;

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
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.enchanting_overhauled.enchanting_group"))
                        .icon(() -> new ItemStack(Items.ENCHANTED_TOME.get()))
                        .displayItems((params, output) -> ItemGroups.displayItems(params, output::accept))
                        .build()
        );

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(
                (entries) -> ItemGroups.populateIngredients(entries.getContext(), entries::accept)
        );

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(
                (entries) -> ItemGroups.populateSpawnEggs(entries::accept)
        );

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(
                (entries) -> {
                    ItemGroups.populateFunctional(entries::accept);
                    // disable the enchanting_table removal
                    if (false && entries instanceof FabricCreativeModeTabOutput fabricEntries) {
                        fabricEntries.getDisplayStacks().removeIf(stack -> stack.is(net.minecraft.world.item.Items.ENCHANTING_TABLE));
                        fabricEntries.getSearchTabStacks().removeIf(stack -> stack.is(net.minecraft.world.item.Items.ENCHANTING_TABLE));
                    }
                }
        );
    }
}