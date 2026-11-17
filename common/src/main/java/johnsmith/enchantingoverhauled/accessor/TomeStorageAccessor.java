package johnsmith.enchantingoverhauled.accessor;

import net.minecraft.world.item.ItemStack;

/**
 * Accessor interface for {@link net.minecraft.world.level.block.entity.EnchantingTableBlockEntity}.
 * <p>
 * This interface is implemented via Mixin to allow the Enchanting Table Block Entity
 * to store an {@link ItemStack}. This is primarily used to hold the "Enchanted Tome"
 * that activates the table, ensuring it persists and can be dropped when the block is broken.
 */
public interface TomeStorageAccessor {

    /**
     * Retrieves the Enchanted Tome item stack currently stored in this block entity.
     *
     * @return The stored ItemStack, or {@link ItemStack#EMPTY} if none is stored.
     */
    ItemStack enchanting_overhauled$getTomeStack();

    /**
     * Sets the Enchanted Tome item stack to be stored in this block entity.
     *
     * @param stack The ItemStack to store.
     */
    void enchanting_overhauled$setTomeStack(ItemStack stack);
}