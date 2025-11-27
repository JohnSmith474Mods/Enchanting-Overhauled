package johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

/**
 * A data-driven power bonus implementation that calculates bonus enchanting power
 * based on the quantity of specific items found inside a neighboring block entity's inventory.
 * <p>
 * This is typically applied to block entities that function as item containers (e.g., Chests)
 * or specialized item storage (e.g., Chiseled Bookshelves).
 *
 * @param items        A {@link HolderSet} representing the item(s) or item tag that should be counted
 * within the target block entity's inventory.
 * @param bonusPerItem The fixed integer amount of enchanting power added for *each* matching item counted.
 */
public record CountItemsInInventoryBonus(
        HolderSet<Item> items,
        int bonusPerItem
) implements PowerBonus {
    /**
     * The codec responsible for serializing and deserializing instances of this record from data files (e.g., JSON).
     * <p>
     * It uses {@link RegistryCodecs#homogeneousList} to correctly handle the {@link HolderSet} of items.
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
     * @return The constant ID string for this bonus type: "count_items_in_inventory".
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