package johnsmith.enchantingoverhauled.item;

import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A custom item representing an Enchanted Tome.
 * <p>
 * The Enchanted Tome is a special variant of an enchanted book used to activate
 * {@link johnsmith.enchantingoverhauled.block.DeactivatedEnchantingTableBlock}s.
 * It extends {@link EnchantedBookItem} to inherit standard enchantment storage behaviors
 * but overrides visual properties like the foil effect.
 */
public class EnchantedTomeItem extends EnchantedBookItem {

    /**
     * Constructs a new EnchantedTomeItem.
     *
     * @param properties The item properties (stack size, rarity, etc.).
     */
    public EnchantedTomeItem(Properties properties) {
        super(properties);
    }

    /**
     * Checks if the item should have the enchantment glint (foil effect).
     * <p>
     * Overridden to always return true, ensuring the Tome always glows regardless of
     * whether it actually has enchantments stored on it yet.
     *
     * @param stack The item stack to check.
     * @return Always true.
     */
    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }
}