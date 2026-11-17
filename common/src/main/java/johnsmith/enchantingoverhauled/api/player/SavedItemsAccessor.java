package johnsmith.enchantingoverhauled.api.player;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Accessor interface implemented via Mixin on the {@code Player} entity class.
 * <p>
 * This interface provides a mechanism to temporarily store and retrieve a list of
 * {@link SavedItemEntry} objects. This system is crucial for implementing custom
 * persistence logic, such as preventing cursed items (e.g., Binding Curse)
 * from being dropped on player death and ensuring they are restored to the player's
 * inventory (specifically their original equipment slots) upon respawn or dimension transfer.
 */
public interface SavedItemsAccessor {
    /**
     * Retrieves the mutable list of items that were successfully saved and should be restored
     * (e.g., items affected by a probabilistic Binding Curse that survived player death).
     *
     * @return A {@code List} of {@link SavedItemEntry} objects.
     */
    List<SavedItemEntry> enchanting_overhauled$getSavedItems();

    /**
     * Adds a new {@link SavedItemEntry} to the list of items pending restoration.
     * <p>
     * This is typically called just before the inventory is cleared on death.
     *
     * @param entry The item and its original slot index to be saved.
     */
    void enchanting_overhauled$addSavedItem(SavedItemEntry entry);
}