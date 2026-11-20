package johnsmith.enchantingoverhauled.mixin.menu;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.api.accessor.AnvilMenuAccessor;

import net.minecraft.core.Holder;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

/**
 * NeoForge's {@code AnvilMenu#createResult} is a final hook wrapper (it fires {@code CommonHooks.onAnvilUpdate});
 * Mojang's body lives in {@code createResultInternal}. The injectors below mirror
 * {@link AnvilMenuCreateResultMixin} against that method and delegate to the common {@link AnvilMenuAccessor} hooks.
 */
@Mixin(AnvilMenu.class)
public abstract class NeoForgeAnvilMenuMixin implements AnvilMenuAccessor {
    @ModifyExpressionValue(
            method = "lambda$onTake$0",
            at = @At(
                    value = "CONSTANT",
                    args = "floatValue=0.12"
            )
    )
    private static float modifyAnvilBreak(final float breakChance) {
        return Config.ANVIL_BREAK_CHANCE.get();
    }

    @WrapOperation(
            method = "createResultInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;supportsEnchantment(Lnet/minecraft/core/Holder;)Z"
            )
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


    // Modify amount of eligible repair items required for item repair (Default is 4).
    @ModifyConstant(method = "createResultInternal", constant = @Constant(intValue = 4))
    private int enchanting_overhauled$maxItemCost(int value) {
        return Config.ANVIL_MAX_ITEM_COST.get();
    }

    // Modify the repair bonus applied when repairing items (Default is 12).
    @ModifyConstant(method = "createResultInternal", constant = @Constant(intValue = 12))
    private int enchanting_overhauled$repairBonus(int value) {
        return Config.ANVIL_REPAIR_BONUS.get();
    }

    // Modify the "Too Expensive!" xp cost when repairing items (Default is 40).
    @ModifyConstant(method = "createResultInternal", constant = @Constant(intValue = 40))
    private int enchanting_overhauled$modifyTooExpensiveXpCost(int value) {
        return Config.ANVIL_MAX_XP_COST.get();
    }

    // Modify the max xp cost when repairing items (Default is 39).
    @ModifyConstant(method = "createResultInternal", constant = @Constant(intValue = 39))
    private int enchanting_overhauled$modifyMaxXpCost(int value) {
        return Config.ANVIL_MAX_XP_COST.get() - 1;
    }

    @ModifyExpressionValue(
            method = "createResultInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;canStoreEnchantments(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean enchanting_overhauled$onCanStoreEnchantments(
            boolean original,
            @Local(ordinal = 0) ItemStack target
    ) {
        return this.enchanting_overhauled$canStoreOrTransmuteEnchantments(original, target);
    }

    @ModifyVariable(
            method = "createResultInternal",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 1
    )
    private ItemStack enchanting_overhauled$onWorkingStackCreated(ItemStack workingStack) {
        return this.enchanting_overhauled$applyEnchantRecipeTarget(workingStack);
    }

    @ModifyVariable(method = "createResultInternal", at = @At("STORE"), ordinal = 2)
    private ItemStack enchanting_overhauled$onSourceStackStored(ItemStack source) {
        return this.enchanting_overhauled$disallowEnchantedBooks(source);
    }

    // NeoForge's patched body reads Abilities#instabuild directly in the per-enchantment loop (vanilla Fabric/Forge
    // use hasInfiniteMaterials() there). The only hasInfiniteMaterials() call left in createResultInternal is the
    // "Too Expensive!" check, which must stay untouched.
    @ModifyExpressionValue(
            method = "createResultInternal",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private boolean enchanting_overhauled$onInstabuild(
            boolean instabuild,
            @Local(ordinal = 1) ItemStack workingStack
    ) {
        return this.enchanting_overhauled$restrictInstabuildArbitraryEnchanting(instabuild, workingStack);
    }

    @ModifyExpressionValue(
            method = "createResultInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentsForCrafting(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;",
                    ordinal = 0
            )
    )
    private ItemEnchantments enchanting_overhauled$onTargetEnchantments(
            ItemEnchantments original,
            @Local(ordinal = 1) ItemStack workingStack
    ) {
        return this.enchanting_overhauled$allowTargetEnchantmentLib(workingStack);
    }

    @ModifyExpressionValue(
            method = "createResultInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentsForCrafting(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;",
                    ordinal = 1
            )
    )
    private ItemEnchantments enchanting_overhauled$onSourceEnchantments(ItemEnchantments original) {
        return this.enchanting_overhauled$allowEnchantRecipeInput();
    }

    @ModifyExpressionValue(
            method = "createResultInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"
            )
    )
    private int enchanting_overhauled$onMaxLevel(
            int original,
            @Local Holder<Enchantment> holder,
            @Local(ordinal = 1) ItemStack workingStack
    ) {
        return this.enchanting_overhauled$allowExceededMaxLevel(original, holder, workingStack);
    }

    @WrapOperation(
            method = "createResultInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;setEnchantments(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/ItemEnchantments;)V"
            )
    )
    private void enchanting_overhauled$onSetEnchantments(
            ItemStack stack,
            ItemEnchantments enchantments,
            Operation<Void> original
    ) {
        this.enchanting_overhauled$setEnchantmentsInCorrectComponent(stack, enchantments, original);
    }

    @Inject(
            method = "createResultInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AnvilMenu;broadcastChanges()V"
            )
    )
    private void enchanting_overhauled$onBroadcastResult(CallbackInfo ci) {
        this.enchanting_overhauled$preventRedundantEnchanting();
    }

    @ModifyVariable(method = "createResultInternal", at = @At("STORE"), ordinal = 0)
    private Iterator<?> enchanting_overhauled$onSourceEnchantmentIterator(Iterator<?> iterator) {
        return this.enchanting_overhauled$disallowEnchantmentCombination(iterator);
    }
}
