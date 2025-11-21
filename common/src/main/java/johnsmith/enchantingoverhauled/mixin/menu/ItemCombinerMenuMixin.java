package johnsmith.enchantingoverhauled.mixin.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemCombinerMenu.class)
public class ItemCombinerMenuMixin {
    // Allow extending classes to access input inventory.
    // Mapped: input -> inputSlots
    @Shadow
    @Final
    protected Container inputSlots;
}