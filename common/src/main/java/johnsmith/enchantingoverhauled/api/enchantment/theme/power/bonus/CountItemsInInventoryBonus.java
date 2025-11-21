package johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

/**
 * A power bonus that counts matching items inside a block entity's inventory.
 * Works for standard Inventories (Chests, Hoppers) and Chiseled Bookshelves.
 *
 * @param items The item, list of items, or tag of items to count.
 * @param bonusPerItem The amount of power to add for each matching item.
 */
public record CountItemsInInventoryBonus(
        HolderSet<Item> items,
        int bonusPerItem
) implements PowerBonus {

    public static final MapCodec<CountItemsInInventoryBonus> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(CountItemsInInventoryBonus::items),
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