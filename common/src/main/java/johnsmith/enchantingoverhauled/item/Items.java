package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.block.Blocks;
import johnsmith.enchantingoverhauled.entity.Entities;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public enum Items implements StringRepresentable {
    DEACTIVATED_ENCHANTING_ALTAR(
            Blocks.DEACTIVATED_ENCHANTING_TABLE.getSerializedName(),
            properties -> new BlockItem(Blocks.DEACTIVATED_ENCHANTING_TABLE.get(), properties),
            new Item.Properties()
    ),
    DISTURBED_ENCHANTING_ALTAR(
            Blocks.DISTURBED_ENCHANTING_TABLE.getSerializedName(),
            properties -> new BlockItem(Blocks.DISTURBED_ENCHANTING_TABLE.get(), properties),
            new Item.Properties()
    ),
    ENCHANTED_TOME(
            "enchanted_tome",
            EnchantedTomeItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()
                    .rarity(Rarity.EPIC)
                    .component(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.DAMAGE_RESISTANT, new DamageResistant(Tags.TOME_IMMUNE_DAMAGE_TYPE))
    ),
    ENCHANTED_PAGE(
            "enchanted_page",
            EnchantedPageItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
                    .component(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
    ),
    ENCHANTED_TOME_SPAWN_EGG(
            Entities.ENCHANTED_TOME.getSerializedName() + "_spawn_egg",
            properties -> new SpawnEggItem(
                    properties.spawnEgg(Entities.ENCHANTED_TOME.get())
            ),
            new Item.Properties()
    ),
    ENCHANTED_BOOK_SPAWN_EGG(
            Entities.ENCHANTED_BOOK.getSerializedName() + "_spawn_egg",
            properties -> new SpawnEggItem(
                    properties.spawnEgg(Entities.ENCHANTED_BOOK.get())
            ),
            new Item.Properties()
    );

    private final String key;
    private final Function<Item.Properties, Item> factory;
    private final Item.Properties properties;
    private Item item;

    Items(String key, Function<Item.Properties, Item> factory, Item.Properties properties) {
        this.key = key;
        this.factory = factory;
        this.properties = properties;
    }

    public Item build() {
        if (this.item == null) this.item = this.factory.apply(
                properties.setId(
                        ResourceKey.create(
                                Registries.ITEM,
                                Identifier.fromNamespaceAndPath(Constants.MOD_ID, key)
                        )
                )
        );
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