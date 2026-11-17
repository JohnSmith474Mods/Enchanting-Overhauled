package johnsmith.enchantingoverhauled.api.player;

import net.minecraft.world.item.ItemStack;

/**
 * A simple data record used to temporarily store a player's item stack along with its
 * original inventory slot index during events such as player death or dimension travel.
 * <p>
 * This is primarily used in conjunction with {@link SavedItemsAccessor} (implemented on the player entity)
 * to ensure items with special retention logic (like Cursed items that shouldn't drop) are
 * successfully returned to the correct inventory slot upon player respawn.
 *
 * @param slot  The integer index of the inventory slot where the item originated (e.g., 36-39 for armor slots).
 * @param stack The {@link ItemStack} that was saved.
 */
public record SavedItemEntry(int slot, ItemStack stack) {}