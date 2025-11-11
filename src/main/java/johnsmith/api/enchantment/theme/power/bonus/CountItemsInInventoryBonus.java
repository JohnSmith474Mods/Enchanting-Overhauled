package johnsmith.api.enchantment.theme.power.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.entry.RegistryEntryList;

/**
 * A power bonus that counts matching items inside a block entity's inventory.
 * Works for standard Inventories (Chests, Hoppers) and Chiseled Bookshelves.
 *
 * @param items The item, list of items, or tag of items to count.
 * @param bonusPerItem The amount of power to add for each matching item.
 */
public record CountItemsInInventoryBonus(
        RegistryEntryList<Item> items,
        int bonusPerItem
) implements PowerBonus {

    public static final MapCodec<CountItemsInInventoryBonus> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    RegistryCodecs.entryList(RegistryKeys.ITEM).fieldOf("items").forGetter(CountItemsInInventoryBonus::items),
                    Codec.INT.fieldOf("bonus_per_item").forGetter(CountItemsInInventoryBonus::bonusPerItem)
            ).apply(instance, CountItemsInInventoryBonus::new)
    );

    @Override
    public String getTypeId() {
        return PowerBonusType.COUNT_ITEMS_IN_INVENTORY.getId();
    }

    @Override
    public MapCodec<? extends PowerBonus> getCodec() {
        return CODEC;
    }
}