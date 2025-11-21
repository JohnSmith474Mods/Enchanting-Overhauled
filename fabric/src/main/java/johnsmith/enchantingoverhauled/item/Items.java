package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.block.Blocks;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Registration handler for items on the Fabric platform.
 * <p>
 * Registers the {@link BlockItem}s for the custom tables and the {@link EnchantedTomeItem}.
 */
public class Items {
    // Instantiate Items here
    public static final Item DEACTIVATED_ENCHANTING_TABLE_ITEM = new BlockItem(Blocks.DEACTIVATED_ENCHANTING_TABLE, new Item.Properties());
    public static final Item DISTURBED_ENCHANTING_TABLE_ITEM = new BlockItem(Blocks.DISTURBED_ENCHANTING_TABLE, new Item.Properties());
    public static final Item ENCHANTED_TOME =
            new EnchantedTomeItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .component(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

    /**
     * Registers all items with the vanilla registry.
     */
    public static void initialize() {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Constants.MOD_ID, "deactivated_enchanting_table"), DEACTIVATED_ENCHANTING_TABLE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Constants.MOD_ID, "disturbed_enchanting_table"), DISTURBED_ENCHANTING_TABLE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Constants.MOD_ID, "enchanted_tome"), ENCHANTED_TOME);
    }
}