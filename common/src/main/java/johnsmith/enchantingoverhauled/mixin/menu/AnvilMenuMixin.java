package johnsmith.enchantingoverhauled.mixin.menu;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.api.accessor.AnvilMenuAccessor;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Loader-agnostic part of the anvil overhaul.
 * <p>
 * This mixin only contains injections whose targets are identical on every loader ({@code mayPickup},
 * {@code getCost}, {@code onTake}) plus the hook implementations from {@link AnvilMenuAccessor}.
 * Injections into the vanilla {@code createResult} body are declared per loader because NeoForge moves that body
 * into {@code createResultInternal}: see {@link AnvilMenuCreateResultMixin} (Fabric/Forge) and
 * {@code NeoForgeAnvilMenuMixin}.
 */
@Mixin(AnvilMenu.class)
public class AnvilMenuMixin extends ItemCombinerMenuMixin implements AnvilMenuAccessor {

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

    // Allow items with STORED_ENCHANTMENTS or a valid EnchantRecipe to enter the anvil evaluation pipeline.
    @Override
    public boolean enchanting_overhauled$canStoreOrTransmuteEnchantments(boolean original, ItemStack target) {
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
    @Override
    public ItemStack enchanting_overhauled$applyEnchantRecipeTarget(ItemStack itemstack1) {
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
    @Override
    public ItemStack enchanting_overhauled$disallowEnchantedBooks(ItemStack itemStack) {
        if (this.inputSlots.getItem(1).has(DataComponents.STORED_ENCHANTMENTS) && !Config.ENABLE_ANVIL_ENCHANTING.get()) {
            return ItemStack.EMPTY;
        }
        return itemStack;
    }

    // Prevent Creative mode (hasInfiniteMaterials) from bypassing canEnchant on raw unenchantable items (like plain books).
    @Override
    public boolean enchanting_overhauled$restrictInstabuildArbitraryEnchanting(boolean instabuild, ItemStack workingStack) {
        if (!instabuild) {
            return false;
        }
        EnchantRecipe recipe = this.enchanting_overhauled$activeRecipe;
        if (recipe != null) {
            return true;
        }
        return workingStack.has(DataComponents.STORED_ENCHANTMENTS) || workingStack.isEnchantable();
    }

    @Override
    public ItemEnchantments enchanting_overhauled$allowTargetEnchantmentLib(ItemStack workingStack) {
        return EnchantmentLib.getEnchantments(workingStack);
    }

    @Override
    public ItemEnchantments enchanting_overhauled$allowEnchantRecipeInput() {
        return EnchantmentLib.getEnchantments(this.inputSlots.getItem(1));
    }

    // Preserve overleveled enchantments from either target or source without allowing further level increments above maxLevel.
    @Override
    public int enchanting_overhauled$allowExceededMaxLevel(int original, Holder<Enchantment> holder, ItemStack workingStack) {
        ItemStack sourceItem = this.inputSlots.getItem(1);
        int sourceLevel = EnchantmentLib.getEnchantments(sourceItem).getLevel(holder);
        int targetLevel = EnchantmentLib.getEnchantments(workingStack).getLevel(holder);
        return Math.max(original, Math.max(sourceLevel, targetLevel));
    }

    // Write merged enchantments to STORED_ENCHANTMENTS for Tomes, Pages, Enchanted Books, and STORED recipes,
    // or strip them for VOID recipes, avoiding duplicate STORED_ENCHANTMENTS + ENCHANTMENTS components.
    @Override
    public void enchanting_overhauled$setEnchantmentsInCorrectComponent(
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
    @Override
    public void enchanting_overhauled$preventRedundantEnchanting() {
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

            // Enforce the global maximum enchantment cap, respecting whether curses count toward the cap
            int maxEnchants = Config.MAX_ENCHANTMENT_AMOUNT.get();
            int targetCapCount = EnchantmentLib.getCapContributingEnchantmentCount(target);
            int resultCapCount = EnchantmentLib.getCapContributingEnchantmentCount(result);

            if (resultCapCount > maxEnchants && resultCapCount > targetCapCount) {
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
    @Override
    public Iterator<?> enchanting_overhauled$disallowEnchantmentCombination(Iterator<?> iterator) {
        if (!Config.ENABLE_ANVIL_ENCHANTING.get()) {
            return Collections.emptyIterator();
        }
        return iterator;
    }

    @Override
    public boolean enchanting_overhauled$canEnchantWorkingStack(
            ItemStack workingStack,
            Predicate<ItemStack> defaultCheck
    ) {
        EnchantRecipe recipe = this.enchanting_overhauled$activeRecipe;
        if (recipe != null) {
            if (recipe.attachment() == EnchantmentAttachment.VOID
                    || recipe.attachment() == EnchantmentAttachment.STORED) {
                return true;
            }
            return defaultCheck.test(workingStack);
        }
        if (workingStack.has(DataComponents.STORED_ENCHANTMENTS)) {
            return true;
        }
        return defaultCheck.test(workingStack);
    }
}