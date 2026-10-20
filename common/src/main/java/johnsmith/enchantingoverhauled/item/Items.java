package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.block.Blocks;
import johnsmith.enchantingoverhauled.entity.Entities;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum Items implements StringRepresentable {
    DEACTIVATED_ENCHANTING_ALTAR(
            Blocks.DEACTIVATED_ENCHANTING_TABLE.getSerializedName(),
            () -> new BlockItem(Blocks.DEACTIVATED_ENCHANTING_TABLE.get(), new Item.Properties())
    ),
    DISTURBED_ENCHANTING_ALTAR(
            Blocks.DISTURBED_ENCHANTING_TABLE.getSerializedName(),
            () -> new BlockItem(Blocks.DISTURBED_ENCHANTING_TABLE.get(), new Item.Properties())
    ),
    ENCHANTED_TOME(
            "enchanted_tome",
            () -> new EnchantedTomeItem(new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()
                    .rarity(Rarity.EPIC)
                    .component(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
    ),
    ENCHANTED_PAGE(
            "enchanted_page",
            () -> new EnchantedPageItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
                    .component(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
    ),
    ENCHANTED_TOME_SPAWN_EGG(
            Entities.ENCHANTED_TOME.getSerializedName() + "_spawn_egg",
            () -> new SpawnEggItem(
                    Entities.ENCHANTED_TOME.get(),
                    0x35222E,
                    0x4D2299,
                    new Item.Properties()
            )
    ),
    ENCHANTED_BOOK_SPAWN_EGG(
            Entities.ENCHANTED_BOOK.getSerializedName() + "_spawn_egg",
            () -> new SpawnEggItem(
                    Entities.ENCHANTED_BOOK.get(),
                    0x44250A,
                    0xC51339,
                    new Item.Properties()
            )
    );

    private final String key;
    private final Supplier<Item> factory;
    private Item item;

    Items(String key, Supplier<Item> factory) {
        this.key = key;
        this.factory = factory;
    }

    public Item build() {
        if (this.item == null) this.item = this.factory.get();
        return this.item;
    }

    public Item get() {
        return this.build();
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.key;
    }
}