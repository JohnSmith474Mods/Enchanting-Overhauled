package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.block.Blocks;

import johnsmith.enchantingoverhauled.damagesource.DamageTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;

/**
 * Registration handler for items on the Fabric platform.
 * <p>
 * Registers the {@link BlockItem}s for the custom tables and the Enchanted Tome {@link Item}.
 */
public class Items {

    public static final ResourceKey<Item> DEACTIVATED_TABLE_KEY = ResourceKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "deactivated_enchanting_table")
    );
    public static final ResourceKey<Item> DISTURBED_TABLE_KEY = ResourceKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "disturbed_enchanting_table")
    );
    public static final ResourceKey<Item> ENCHANTED_TOME_KEY = ResourceKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_tome")
    );

    // Instantiate Items here
    public static final Item DEACTIVATED_ENCHANTING_TABLE_ITEM = new BlockItem(Blocks.DEACTIVATED_ENCHANTING_TABLE, new Item.Properties().setId(DEACTIVATED_TABLE_KEY));
    public static final Item DISTURBED_ENCHANTING_TABLE_ITEM = new BlockItem(Blocks.DISTURBED_ENCHANTING_TABLE, new Item.Properties().setId(DISTURBED_TABLE_KEY));
    public static final Item ENCHANTED_TOME = new Item(new Item.Properties()
            .setId(ENCHANTED_TOME_KEY)
            .stacksTo(1)
            .component(DataComponents.DAMAGE_RESISTANT, new DamageResistant(DamageTypeRegistry.IGNORES_TOMES))
            .fireResistant()
            .rarity(Rarity.EPIC)
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
            .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_tome")))
    );

    /**
     * Registers all items with the vanilla registry.
     */
    public static void initialize() {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "deactivated_enchanting_table"), DEACTIVATED_ENCHANTING_TABLE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "disturbed_enchanting_table"), DISTURBED_ENCHANTING_TABLE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_tome"), ENCHANTED_TOME);
    }
}