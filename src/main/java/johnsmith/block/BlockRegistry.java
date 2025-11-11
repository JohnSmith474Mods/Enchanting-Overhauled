package johnsmith.block;

import johnsmith.EnchantingOverhauled;
import johnsmith.item.ItemRegistry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockRegistry {
    // Define the new blocks
    public static final Block DEACTIVATED_ENCHANTING_TABLE = register(
        "deactivated_enchanting_table",
        new DeactivatedEnchantingTableBlock(
                AbstractBlock.Settings.copy(Blocks.ENCHANTING_TABLE)
                .requiresTool()
                .luminance((state) -> 0)
                .strength(5.0F, 1200.0F)
                .nonOpaque())
    );
    public static final Block DISTURBED_ENCHANTING_TABLE = register(
        "disturbed_enchanting_table",
        new DeactivatedEnchantingTableBlock(
                AbstractBlock.Settings.copy(Blocks.ENCHANTING_TABLE)
               .requiresTool()
               .luminance((state) -> 7)
               .strength(5.0F, 1200.0F)
               .nonOpaque())
    );

    public static Block register(String id, Block block) {
        Identifier blockKey = new Identifier(EnchantingOverhauled.MOD_ID, id);
        Block registeredBlock = Registry.register(Registries.BLOCK, blockKey, block);
        ItemRegistry.register(id, new BlockItem(registeredBlock, new Item.Settings()));
        return registeredBlock;
    }

    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing block registry...");

        // Add to creative tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(DEACTIVATED_ENCHANTING_TABLE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(DISTURBED_ENCHANTING_TABLE);
        });
    }
}