package johnsmith.enchantingoverhauled.mixin.menu;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import johnsmith.enchantingoverhauled.api.accessor.AnvilMenuAccessor;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
public abstract class ForgeAnvilMenuMixin implements AnvilMenuAccessor {

    @WrapOperation(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean enchanting_overhauled$checkCanEnchantWorkingStack(
            Enchantment enchantment,
            ItemStack originalTarget,
            Operation<Boolean> original,
            @Local(ordinal = 1) ItemStack workingStack
    ) {
        return this.enchanting_overhauled$canEnchantWorkingStack(
                workingStack,
                stack -> original.call(enchantment, stack)
        );
    }
}
