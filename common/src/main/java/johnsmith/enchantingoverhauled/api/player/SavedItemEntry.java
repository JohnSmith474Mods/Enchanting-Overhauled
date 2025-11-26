package johnsmith.enchantingoverhauled.api.player;

import net.minecraft.world.item.ItemStack;

public record SavedItemEntry(int slot, ItemStack stack) {}