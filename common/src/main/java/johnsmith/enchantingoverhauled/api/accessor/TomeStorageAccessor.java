package johnsmith.enchantingoverhauled.api.accessor;

import net.minecraft.world.item.ItemStack;

/**
 * Accessor interface for {@link net.minecraft.world.level.block.entity.EnchantingTableBlockEntity}.
 * <p>
 * This interface is implemented via Mixin to allow the vanilla Enchanting Table Block Entity
 * to store an {@link ItemStack}. It is used to persist the specific {@code Enchanted Tome}
 * that was used to activate a {@link johnsmith.enchantingoverhauled.block.DeactivatedEnchantingTableBlock},
 * ensuring the exact tome (along with its stored enchantments) can be retrieved via pick-block
 * or dropped when the table is broken.
 */
public interface TomeStorageAccessor {

    /**
     * Retrieves the Enchanted Tome item stack currently stored inside this block entity.
     *
     * @return The stored {@link ItemStack}, or {@link ItemStack#EMPTY} if no tome is stored.
     */
    ItemStack enchanting_overhauled$getTomeStack();

    /**
     * Sets the Enchanted Tome item stack to be stored in this block entity and marks the
     * block entity for persistence and client synchronization.
     *
     * @param stack The {@link ItemStack} representing the Enchanted Tome to store.
     */
    void enchanting_overhauled$setTomeStack(ItemStack stack);
}