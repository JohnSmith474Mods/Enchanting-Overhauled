package johnsmith.enchantingoverhauled.mixin.menu;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import johnsmith.enchantingoverhauled.api.accessor.AnvilMenuAccessor;

import net.minecraft.core.Holder;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
public abstract class NeoForgeAnvilMenuMixin implements AnvilMenuAccessor {

    // NeoForge 21.0.x (MC 1.21): AnvilMenu#createResult calls Enchantment#canEnchant(ItemStack).
    @WrapOperation(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"
            ),
            require = 0,
            expect = 0
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

    // NeoForge 21.1.x (MC 1.21.1): NeoForge PR #1412 replaced Enchantment#canEnchant with ItemStack#supportsEnchantment(Holder).
    @WrapOperation(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;supportsEnchantment(Lnet/minecraft/core/Holder;)Z"
            ),
            require = 0,
            expect = 0
    )
    private boolean enchanting_overhauled$checkSupportsEnchantmentWorkingStack(
            ItemStack originalTarget,
            Holder<Enchantment> holder,
            Operation<Boolean> original,
            @Local(ordinal = 1) ItemStack workingStack
    ) {
        return this.enchanting_overhauled$canEnchantWorkingStack(
                workingStack,
                stack -> original.call(stack, holder)
        );
    }
}
