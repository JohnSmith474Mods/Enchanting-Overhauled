package johnsmith.enchantingoverhauled.menu.scribe.controller;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipe;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGameState;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGridData;
import johnsmith.enchantingoverhauled.api.config.type.AbortionRefundPolicy;
import johnsmith.enchantingoverhauled.api.tag.Tags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

import java.util.Optional;

/**
 * Encapsulates the pure domain logic, mathematics, and rule validations for the Scribe Mini-game.
 * Strictly separates game evaluation from the side-effecting mutations found in {@link ScribeAction}.
 */
public class ScribeRules {

    /**
     * Evaluates the current state of the source item and the target book on the lectern
     * to determine if a game is permitted to start, or if constraints (max enchantments) are violated.
     */
    public static ScribeGameState evaluateState(Level level, BlockPos pos, ItemStack sourceStack, ScribeGridData gridData) {
        if (sourceStack.isEmpty()) return ScribeGameState.MISSING_SOURCE;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof LecternBlockEntity lectern)) return ScribeGameState.MISSING_SOURCE;

        ItemStack book = lectern.getBook();
        if (book.isEmpty()) return ScribeGameState.MISSING_SOURCE;

        int selectedId = gridData.getSelectedEnchantmentIndex();
        if (selectedId < 0) return ScribeGameState.MISSING_SOURCE;

        ItemEnchantments sourceEnchants = EnchantmentLib.getEnchantments(sourceStack);
        if (sourceEnchants.isEmpty()) return ScribeGameState.MISSING_SOURCE;

        Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> sourceEnch = null;
        int sourceLevel = 0;

        for (var entry : sourceEnchants.entrySet()) {
            if (registry.getId(entry.getKey().value()) == selectedId) {
                sourceEnch = entry.getKey();
                sourceLevel = entry.getIntValue();
                break;
            }
        }

        if (sourceEnch == null) return ScribeGameState.MISSING_SOURCE;

        ItemEnchantments targetEnchants = EnchantmentLib.getEnchantments(book);
        int targetLevel = targetEnchants.getLevel(sourceEnch);
        int maxEnchants = Config.MAX_ENCHANTMENT_AMOUNT.get();

        boolean isTome = book.is(Items.ENCHANTED_TOME.get());

        if (targetLevel == 0) {
            if (targetEnchants.size() >= maxEnchants) {
                return ScribeGameState.SATURATED;
            }
        } else {
            int vanillaMax = sourceEnch.value().getMaxLevel();
            boolean willBeTome = isTome || willConvertOnUpgrade(targetLevel + sourceLevel, vanillaMax);
            int absoluteMax = (willBeTome && Config.CAN_TOMES_HAVE_GREATER_ENCHANTMENTS.get()) ? vanillaMax + 1 : vanillaMax;

            if (targetLevel >= absoluteMax) {
                return ScribeGameState.MAXIMIZED;
            }
        }

        return ScribeGameState.READY;
    }

    /**
     * Determines whether an item should upgrade to an Enchanted Tome based on the active upgrade configuration.
     */
    public static boolean willConvertOnUpgrade(int newLevel, int vanillaMax) {
        return switch (Config.SCRIBE_TOME_CONVERSION_UPGRADE_CONDITION.get()) {
            case NONE -> true;
            case CUSTOM -> newLevel >= Config.SCRIBE_CUSTOM_TOME_CONVERSION_UPGRADE_LEVEL.get();
            case MAXED -> newLevel >= vanillaMax;
            case OVERLEVELED -> newLevel > vanillaMax;
            default -> false;
        };
    }

    /**
     * Determines whether an item should upgrade to an Enchanted Tome based on the active append configuration.
     */
    public static boolean willConvertOnAppend(int newAmount) {
        return switch (Config.SCRIBE_TOME_CONVERSION_APPEND_CONDITION.get()) {
            case CUSTOM -> newAmount >= Config.SCRIBE_CUSTOM_TOME_CONVERSION_APPEND_AMOUNT.get();
            case CAP -> newAmount >= Config.MAX_ENCHANTMENT_AMOUNT.get();
            default -> false;
        };
    }

    /**
     * Dynamically calculates the required mine density based on the target book's existing enchantments.
     */
    public static void calculateDifficulty(ItemStack book, ScribeGridData gridData) {
        ItemEnchantments targetEnchants = EnchantmentLib.getEnchantments(book);
        int totalLevels = 0;
        for (var entry : targetEnchants.entrySet()) {
            totalLevels += entry.getIntValue();
        }

        int baseMines = 6;
        int mineScalar = Config.SCRIBE_MINE_SCALAR.get();
        int maxDensityMines = ScribeGridData.TOTAL_CELLS * 30 / 100;
        int calculatedMines = baseMines + (totalLevels * mineScalar);
        int activeMines = Math.min(calculatedMines, maxDensityMines);

        gridData.setMineCount(activeMines);
    }

    /**
     * Dynamically calculates the required lapis and XP to process the enchantment binding.
     */
    public static void calculateCosts(ScribeGridData gridData) {
        int mines = gridData.getMineCount();
        float fuelScalar = Config.SCRIBE_FUEL_COST_SCALAR.get();
        int lapisCost;

        if (fuelScalar <= 0.0F) {
            lapisCost = 1;
        } else {
            lapisCost = Math.max(1, (int) (mines * fuelScalar));
        }

        lapisCost = Math.min(lapisCost, 64);
        gridData.setLapisCost(lapisCost);

        int xpReward = mines * Config.SCRIBE_XP_REWARD_SCALAR.get();
        gridData.setXpReward(xpReward);
    }

    /**
     * Validates whether a source item should be prematurely processed at the start of a Scribe game.
     */
    public static boolean isProcessedAtStart(ItemStack source) {
        if (source.is(Tags.PERSISTENT_ENCHANTMENT_SOURCE)) return true; // Allows bypassing termination loss

        AbortionRefundPolicy policy = Config.SCRIBE_ABORTION_REFUND_POLICY.get();
        boolean isFleeting = source.is(Tags.FLEETING_ENCHANTMENT_SOURCE);

        if (policy == AbortionRefundPolicy.NONE) return true;
        if (policy == AbortionRefundPolicy.UNTAGGED) return isFleeting;

        return false;
    }

    /**
     * Handles the safe extraction of an enchantment from a source catalyst without destroying it fully,
     * unless the item is deemed fleeting or completely depleted.
     */
    public static void processSourceInPlace(Level level, Container container, ScribeGridData gridData, ItemStack source) {
        if (source.isEmpty() || source.is(Tags.PERSISTENT_ENCHANTMENT_SOURCE)) return;

        int selectedId = gridData.getSelectedEnchantmentIndex();
        Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> selectedEnch = null;

        for (var entry : EnchantmentLib.getEnchantments(source).entrySet()) {
            if (registry.getId(entry.getKey().value()) == selectedId) {
                selectedEnch = entry.getKey();
                break;
            }
        }

        if (selectedEnch != null) {
            ItemStack processed = processSourceConsumption(level, source, selectedEnch);
            container.setItem(0, processed);
        }
    }

    private static ItemStack processSourceConsumption(Level level, ItemStack source, Holder<Enchantment> consumedEnchantment) {
        boolean isFleeting = source.is(Tags.FLEETING_ENCHANTMENT_SOURCE);
        ItemEnchantments enchants = EnchantmentLib.getEnchantments(source);

        if (isFleeting || enchants.size() <= 1) {
            return revertSourceItem(level, source);
        } else {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchants);
            mutable.removeIf(holder -> holder.equals(consumedEnchantment));
            ItemStack copy = source.copy();
            copy.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
            return copy;
        }
    }

    private static ItemStack revertSourceItem(Level level, ItemStack source) {
        Optional<EnchantRecipe> recipeOpt = EnchantRecipe.reverse(level.registryAccess(), source);
        if (recipeOpt.isPresent()) {
            Optional<Holder<Item>> firstInput = recipeOpt.get().input().stream().findFirst();
            if (firstInput.isPresent()) {
                return new ItemStack(firstInput.get().value(), source.getCount());
            }
        }
        ItemStack copy = source.copy();
        copy.remove(DataComponents.STORED_ENCHANTMENTS);
        return copy;
    }
}