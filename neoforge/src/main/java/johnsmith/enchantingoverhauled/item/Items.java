package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.block.Blocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration handler for items on the NeoForge platform.
 * <p>
 * Defines {@link DeferredHolder}s for the mod's items and registers them via the event bus.
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

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, Constants.MOD_ID);

    public static final DeferredHolder<Item, Item> DEACTIVATED_ENCHANTING_TABLE_ITEM =
            ITEMS.register("deactivated_enchanting_table", () -> new BlockItem(Blocks.DEACTIVATED_ENCHANTING_TABLE.get(), new Item.Properties().setId(DEACTIVATED_TABLE_KEY)));

    public static final DeferredHolder<Item, Item> DISTURBED_ENCHANTING_TABLE_ITEM =
            ITEMS.register("disturbed_enchanting_table", () -> new BlockItem(Blocks.DISTURBED_ENCHANTING_TABLE.get(), new Item.Properties().setId(DISTURBED_TABLE_KEY)));

    public static final DeferredHolder<Item, Item> ENCHANTED_TOME =
            ITEMS.register("enchanted_tome", () -> new Item(new Item.Properties()
                    .setId(ENCHANTED_TOME_KEY)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_tome")))
            ));

    /**
     * Initializes the item register.
     *
     * @param eventBus The mod event bus.
     */
    public static void initialize(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}