package johnsmith.enchantingoverhauled.api.player;

import java.util.List;

public interface SavedItemsAccessor {
    List<SavedItemEntry> enchanting_overhauled$getSavedItems();
    void enchanting_overhauled$addSavedItem(SavedItemEntry entry);
}