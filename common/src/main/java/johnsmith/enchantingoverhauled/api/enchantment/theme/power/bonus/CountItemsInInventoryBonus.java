package johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

/**
 * A data-driven {@link PowerBonus} implementation that calculates bonus enchanting power
 * based on the quantity of specific items found inside a power-providing block entity's inventory.
 * <p>
 * Typically applied to block entities that implement {@link net.minecraft.world.Container},
 * such as Chiseled Bookshelves holding Enchanted Books.
 *
 * @param items        A {@link HolderSet} representing the item(s) or item tag that should be counted
 *                     within the target block entity's inventory.
 * @param bonusPerItem The fixed integer amount of enchanting power added for <em>each</em> matching item counted.
 */
public record CountItemsInInventoryBonus(
        HolderSet<Item> items,
        int bonusPerItem
) implements PowerBonus {

    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     * <p>
     * Uses {@link RegistryCodecs#homogeneousList} to handle the {@link HolderSet} of items.
     */
    public static final MapCodec<CountItemsInInventoryBonus> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(CountItemsInInventoryBonus::items),
                    Codec.INT.fieldOf("bonus_per_item").forGetter(CountItemsInInventoryBonus::bonusPerItem)
            ).apply(instance, CountItemsInInventoryBonus::new)
    );

    /**
     * {@inheritDoc}
     *
     * @return The constant ID string for this bonus type: {@code "count_items_in_inventory"}.
     */
    @Override
    public String getTypeId() {
        return PowerBonusType.COUNT_ITEMS_IN_INVENTORY.getId();
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@link MapCodec} for this bonus type.
     */
    @Override
    public MapCodec<? extends PowerBonus> getCodec() {
        return CODEC;
    }
}