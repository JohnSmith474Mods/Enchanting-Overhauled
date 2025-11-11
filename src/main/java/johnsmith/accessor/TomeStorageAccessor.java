package johnsmith.accessor;

import net.minecraft.item.ItemStack;

public interface TomeStorageAccessor {
    ItemStack enchanting_overhauled$getTomeStack();

    void enchanting_overhauled$setTomeStack(ItemStack stack);
}
