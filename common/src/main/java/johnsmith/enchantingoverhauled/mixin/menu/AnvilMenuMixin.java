package johnsmith.enchantingoverhauled.mixin.menu;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import johnsmith.enchantingoverhauled.config.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Iterator;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin extends ItemCombinerMenuMixin {
    // Override to allow taking output regardless of level cost.
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void mayPickup(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    // Override to remove level cost.
    @Inject(method = "getCost", at = @At("HEAD"), cancellable = true)
    public void getCost(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }

    // Remove experience level deduction on output take.
    @WrapOperation(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"))
    protected void noRepairCostInject(final Player instance, final int levels, final Operation<Void> original) {
        // No-op
    }

    // Modify amount of eligible repair items required for item repair.
    // Default is 4.
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 4))
    private int maxItemCost(int value) {
        return Config.BOUNDED_ANVIL_MAX_ITEM_COST.get();
    }

    // Modify the repair bonus applied when repairing items.
    // Default is 12.
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 12))
    private int repairBonus(int value) {
        return Config.BOUNDED_ANVIL_REPAIR_BONUS.get();
    }

    // Disallow using enchanted books in anvil combinations.
    @ModifyVariable(method = "createResult", at = @At("STORE"), ordinal = 2)
    private ItemStack disallowEnchantedBooks(ItemStack itemStack) {
        // 'inputSlots' is the Mojang name for the inventory field in ItemCombinerMenu
        if (this.inputSlots.getItem(1).getItem() instanceof EnchantedBookItem) {
            return ItemStack.EMPTY;
        } else {
            return itemStack;
        }
    }

    // Disallow combining enchantments from the second input item.
    @ModifyVariable(method = "createResult", at = @At("STORE"), ordinal = 0)
    private Iterator<?> disallowEnchantmentCombination(Iterator<?> itemStack) {
        return Collections.emptyIterator();
    }
}