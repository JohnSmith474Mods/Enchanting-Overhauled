package johnsmith.enchantingoverhauled.mixin.menu;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Targets the anonymous inner class 'ResultSlot' implemented within GrindstoneMenu
 * to intercept and fix the XP calculation for custom arbitrary items.
 */
@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
public class GrindstoneMenuResultSlotMixin {

    @Inject(method = "getExperienceFromItem", at = @At("HEAD"), cancellable = true)
    private void enchanting_overhauled$calculateCustomXp(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        // Interfere only if the item uses STORED_ENCHANTMENTS but isn't a vanilla book
        if (stack.has(DataComponents.STORED_ENCHANTMENTS) && !stack.is(Items.ENCHANTED_BOOK)) {
            int xp = 0;
            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

            for(Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                Holder<Enchantment> holder = entry.getKey();
                int level = entry.getIntValue();

                // Grindstones calculate XP strictly from non-cursed enchantments
                if (!holder.is(EnchantmentTags.CURSE)) {
                    xp += holder.value().getMinCost(level);
                }
            }

            cir.setReturnValue(xp);
        }
    }
}