package johnsmith.enchantingoverhauled.api.accessor;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public interface AnvilMenuAccessor {
    boolean enchanting_overhauled$canEnchantWorkingStack(ItemStack workingStack, Predicate<ItemStack> defaultCheck);
}