package johnsmith.enchantingoverhauled.mixin.menu;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipe;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantmentAttachment;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.api.tag.Tags;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin extends ItemCombinerMenuMixin {

    @Shadow
    @Final
    private DataSlot cost;

    @Shadow
    private String itemName;

    @Shadow
    private int repairItemCountCost;

    @Unique
    @Nullable
    private EnchantRecipe enchanting_overhauled$activeRecipe = null;

    // Override to allow taking output regardless of level cost.
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void enchanting_overhauled$mayPickup(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.ENABLE_ANVIL_XP_COST.get()) {
            cir.setReturnValue(true);
        }
    }

    // Override to remove level cost.
    @Inject(method = "getCost", at = @At("HEAD"), cancellable = true)
    public void enchanting_overhauled$getCost(CallbackInfoReturnable<Integer> cir) {
        if (!Config.ENABLE_ANVIL_XP_COST.get()) {
            cir.setReturnValue(0);
        }
    }

    // Remove experience level deduction on output take.
    @WrapOperation(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"))
    protected void enchanting_overhauled$noRepairCostInject(Player instance, int levels, Operation<Void> original) {
        if (Config.ENABLE_ANVIL_XP_COST.get()) {
            original.call(instance, levels);
        }
    }

    // Modify amount of eligible repair items required for item repair (Default is 4).
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 4))
    private int enchanting_overhauled$maxItemCost(int value) {
        return Config.ANVIL_MAX_ITEM_COST.get();
    }

    // Modify the repair bonus applied when repairing items (Default is 12).
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 12))
    private int enchanting_overhauled$repairBonus(int value) {
        return Config.ANVIL_REPAIR_BONUS.get();
    }

    // Modify the "Too Expensive!" xp cost when repairing items (Default is 40).
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40))
    private int enchanting_overhauled$modifyTooExpensiveXpCost(int value) {
        return Config.ANVIL_MAX_XP_COST.get();
    }

    // Modify the max xp cost when repairing items (Default is 39).
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 39))
    private int enchanting_overhauled$modifyMaxXpCost(int value) {
        return Config.ANVIL_MAX_XP_COST.get() - 1;
    }

    // Allow items with STORED_ENCHANTMENTS or a valid EnchantRecipe to enter the anvil evaluation pipeline.
    @ModifyExpressionValue(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;canStoreEnchantments(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean enchanting_overhauled$canStoreOrTransmuteEnchantments(
            boolean original,
            @Local(ordinal = 0) ItemStack target
    ) {
        this.enchanting_overhauled$activeRecipe = null;
        if (target.isEmpty()) {
            return false;
        }
        ItemStack source = this.inputSlots.getItem(1);
        if (Config.ENABLE_ANVIL_ENCHANTING.get()
                && !source.isEmpty()
                && source.has(DataComponents.STORED_ENCHANTMENTS)
                && !EnchantmentLib.getEnchantments(source).isEmpty()
                && this.player != null) {
            Optional<EnchantRecipe> recipeOpt = EnchantRecipe.get(this.player.level().registryAccess(), target);
            if (recipeOpt.isPresent()) {
                this.enchanting_overhauled$activeRecipe = recipeOpt.get();
                return true;
            }
        }
        return original || target.has(DataComponents.STORED_ENCHANTMENTS);
    }

    // Transmute the working result copy (itemstack1) when an EnchantRecipe is active.
    @ModifyVariable(
            method = "createResult",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 1
    )
    private ItemStack enchanting_overhauled$applyEnchantRecipeTarget(ItemStack itemstack1) {
        EnchantRecipe recipe = this.enchanting_overhauled$activeRecipe;
        if (recipe == null) {
            return itemstack1;
        }
        ItemStack transmuted = new ItemStack(recipe.output().value(), itemstack1.getCount());
        if (itemstack1.has(DataComponents.CUSTOM_NAME)) {
            transmuted.set(DataComponents.CUSTOM_NAME, itemstack1.get(DataComponents.CUSTOM_NAME));
        }
        if (itemstack1.has(DataComponents.REPAIR_COST)) {
            transmuted.set(DataComponents.REPAIR_COST, itemstack1.get(DataComponents.REPAIR_COST));
        }

        ItemEnchantments existingEnchants = EnchantmentLib.getEnchantments(itemstack1);
        if (recipe.attachment() == EnchantmentAttachment.STORED) {
            transmuted.set(DataComponents.STORED_ENCHANTMENTS, existingEnchants);
        } else if (recipe.attachment() != EnchantmentAttachment.VOID && !existingEnchants.isEmpty()) {
            transmuted.set(DataComponents.ENCHANTMENTS, existingEnchants);
        }
        return transmuted;
    }

    // Disallow using stored enchantment sources (books/pages/tomes) when ENABLE_ANVIL_ENCHANTING is false.
    @ModifyVariable(method = "createResult", at = @At("STORE"), ordinal = 2)
    private ItemStack enchanting_overhauled$disallowEnchantedBooks(ItemStack itemStack) {
        if (this.inputSlots.getItem(1).has(DataComponents.STORED_ENCHANTMENTS) && !Config.ENABLE_ANVIL_ENCHANTING.get()) {
            return ItemStack.EMPTY;
        }
        return itemStack;
    }

    // Check enchantment applicability against the working output item (itemstack1) or active EnchantRecipe.
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
        EnchantRecipe recipe = this.enchanting_overhauled$activeRecipe;
        if (recipe != null) {
            if (recipe.attachment() == EnchantmentAttachment.VOID
                    || recipe.attachment() == EnchantmentAttachment.STORED) {
                return true;
            }
            return original.call(enchantment, workingStack);
        }
        if (workingStack.has(DataComponents.STORED_ENCHANTMENTS)) {
            return true;
        }
        return original.call(enchantment, workingStack);
    }

    // Target ONLY ordinal 1 of ItemStack.is(Item) (itemstack.is(Items.ENCHANTED_BOOK) on line 154)
    // and check the working target stack (itemstack1) rather than slot 1!
    @ModifyExpressionValue(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z",
                    ordinal = 1
            )
    )
    private boolean enchanting_overhauled$allowArbitraryEnchantmentStorage(
            final boolean state,
            @Local(ordinal = 1) ItemStack workingStack
    ) {
        EnchantRecipe recipe = this.enchanting_overhauled$activeRecipe;
        if (recipe != null && (recipe.attachment() == EnchantmentAttachment.STORED || recipe.attachment() == EnchantmentAttachment.VOID)) {
            return true;
        }
        return state || workingStack.has(DataComponents.STORED_ENCHANTMENTS);
    }

    // Prevent Creative mode (instabuild) from bypassing canEnchant on raw unenchantable items (like plain books).
    @ModifyExpressionValue(
            method = "createResult",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z",
                    ordinal = 0
            )
    )
    private boolean enchanting_overhauled$restrictInstabuildArbitraryEnchanting(
            boolean instabuild,
            @Local(ordinal = 1) ItemStack workingStack
    ) {
        if (!instabuild) {
            return false;
        }
        EnchantRecipe recipe = this.enchanting_overhauled$activeRecipe;
        if (recipe != null) {
            return true;
        }
        return workingStack.has(DataComponents.STORED_ENCHANTMENTS) || workingStack.isEnchantable();
    }

    // Prevent persistent Enchantment Storage from being consumed.
    @WrapOperation(
            method = "onTake",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void enchanting_overhauled$preservePersistentEnchantmentStorage(
            Container container,
            int slot,
            ItemStack itemStack,
            Operation<Void> operation
    ) {
        if ((slot == 1 && itemStack.isEmpty() && !this.inputSlots.getItem(slot).is(Tags.PERSISTENT_ENCHANTMENT_SOURCE))
                || slot == 0) {
            operation.call(container, slot, itemStack);
        }
    }

    @ModifyExpressionValue(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentsForCrafting(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;",
                    ordinal = 0
            )
    )
    private ItemEnchantments enchanting_overhauled$allowTargetEnchantmentLib(
            ItemEnchantments original,
            @Local(ordinal = 1) ItemStack workingStack
    ) {
        return EnchantmentLib.getEnchantments(workingStack);
    }

    @ModifyExpressionValue(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentsForCrafting(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;",
                    ordinal = 1
            )
    )
    private ItemEnchantments enchanting_overhauled$allowEnchantRecipeInput(ItemEnchantments original) {
        return EnchantmentLib.getEnchantments(this.inputSlots.getItem(1));
    }

    // Preserve overleveled enchantments from either target or source without allowing further level increments above maxLevel.
    @ModifyExpressionValue(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"
            )
    )
    private int enchanting_overhauled$allowExceededMaxLevel(
            int original,
            @Local Holder<Enchantment> holder,
            @Local(ordinal = 1) ItemStack workingStack
    ) {
        ItemStack sourceItem = this.inputSlots.getItem(1);
        int sourceLevel = EnchantmentLib.getEnchantments(sourceItem).getLevel(holder);
        int targetLevel = EnchantmentLib.getEnchantments(workingStack).getLevel(holder);
        return Math.max(original, Math.max(sourceLevel, targetLevel));
    }

    // Write merged enchantments to STORED_ENCHANTMENTS for Tomes, Pages, Enchanted Books, and STORED recipes,
    // or strip them for VOID recipes, avoiding duplicate STORED_ENCHANTMENTS + ENCHANTMENTS components.
    @WrapOperation(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;setEnchantments(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/ItemEnchantments;)V"
            )
    )
    private void enchanting_overhauled$setEnchantmentsInCorrectComponent(
            ItemStack stack,
            ItemEnchantments enchantments,
            Operation<Void> original
    ) {
        EnchantRecipe recipe = this.enchanting_overhauled$activeRecipe;
        if (recipe != null && recipe.attachment() == EnchantmentAttachment.VOID) {
            stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            stack.remove(DataComponents.STORED_ENCHANTMENTS);
            return;
        }
        if ((recipe != null && recipe.attachment() == EnchantmentAttachment.STORED)
                || stack.has(DataComponents.STORED_ENCHANTMENTS)) {
            stack.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
            stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            return;
        }
        original.call(stack, enchantments);
    }

    // Cancel no-op / redundant anvil combinations (e.g., when both target and source are already at or above maxLevel).
    @Inject(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AnvilMenu;broadcastChanges()V"
            )
    )
    private void enchanting_overhauled$preventRedundantEnchanting(CallbackInfo ci) {
        this.enchanting_overhauled$activeRecipe = null;
        ItemStack target = this.inputSlots.getItem(0);
        ItemStack source = this.inputSlots.getItem(1);
        ItemStack result = this.resultSlots.getItem(0);

        if (!target.isEmpty() && !source.isEmpty() && !result.isEmpty()) {
            // Disallow enchanting stacked items (e.g. >1 book in Creative mode) unless performing material durability repair
            if (target.getCount() > 1 && this.repairItemCountCost == 0) {
                this.resultSlots.setItem(0, ItemStack.EMPTY);
                this.cost.set(0);
                return;
            }

            boolean itemTransmuted = !target.is(result.getItem());
            boolean durabilityRepaired = target.getDamageValue() != result.getDamageValue();
            ItemEnchantments targetEnchants = EnchantmentLib.getEnchantments(target);
            ItemEnchantments resultEnchants = EnchantmentLib.getEnchantments(result);

            if (!itemTransmuted && !durabilityRepaired && targetEnchants.equals(resultEnchants)) {
                this.resultSlots.setItem(0, ItemStack.EMPTY);
                this.cost.set(0);
            }
        }
    }

    // Disallow combining enchantments from the second input item when ENABLE_ANVIL_ENCHANTING is false.
    @ModifyVariable(method = "createResult", at = @At("STORE"), ordinal = 0)
    private Iterator<?> enchanting_overhauled$disallowEnchantmentCombination(Iterator<?> iterator) {
        if (!Config.ENABLE_ANVIL_ENCHANTING.get()) {
            return Collections.emptyIterator();
        }
        return iterator;
    }
}