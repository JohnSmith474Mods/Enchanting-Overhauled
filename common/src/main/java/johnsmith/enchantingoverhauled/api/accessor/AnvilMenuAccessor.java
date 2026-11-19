package johnsmith.enchantingoverhauled.api.accessor;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Hook surface implemented by the common {@code AnvilMenuMixin}.
 * <p>
 * The vanilla {@code AnvilMenu#createResult} body lives in different methods depending on the loader
 * (NeoForge extracts it into {@code createResultInternal}), so the injectors that target that body are declared
 * per loader and delegate their logic to the methods below.
 */
public interface AnvilMenuAccessor {
    boolean enchanting_overhauled$canEnchantWorkingStack(ItemStack workingStack, Predicate<ItemStack> defaultCheck);

    /**
     * Decides whether the target item may enter the anvil evaluation pipeline (STORED_ENCHANTMENTS holders or a
     * valid {@code EnchantRecipe}). Resets and re-resolves the active recipe.
     */
    boolean enchanting_overhauled$canStoreOrTransmuteEnchantments(boolean original, ItemStack target);

    /** Transmutes the working result copy when an {@code EnchantRecipe} is active. */
    ItemStack enchanting_overhauled$applyEnchantRecipeTarget(ItemStack workingStack);

    /** Blocks stored-enchantment sources when anvil enchanting is disabled. */
    ItemStack enchanting_overhauled$disallowEnchantedBooks(ItemStack source);

    /** Restricts creative-mode bypass of {@code canEnchant} to enchantable / stored-enchantment items. */
    boolean enchanting_overhauled$restrictInstabuildArbitraryEnchanting(boolean instabuild, ItemStack workingStack);

    /** Enchantments of the working stack, including stored enchantments. */
    ItemEnchantments enchanting_overhauled$allowTargetEnchantmentLib(ItemStack workingStack);

    /** Enchantments of the second input slot, including stored enchantments. */
    ItemEnchantments enchanting_overhauled$allowEnchantRecipeInput();

    /** Preserves overleveled enchantments from either input without allowing further increments. */
    int enchanting_overhauled$allowExceededMaxLevel(int original, Holder<Enchantment> holder, ItemStack workingStack);

    /** Writes merged enchantments into the correct component (STORED / VOID / default). */
    void enchanting_overhauled$setEnchantmentsInCorrectComponent(
            ItemStack stack,
            ItemEnchantments enchantments,
            Operation<Void> original
    );

    /** Cancels no-op or cap-violating anvil results right before the menu broadcasts them. */
    void enchanting_overhauled$preventRedundantEnchanting();

    /** Disables combining enchantments from the second input when anvil enchanting is disabled. */
    Iterator<?> enchanting_overhauled$disallowEnchantmentCombination(Iterator<?> iterator);
}