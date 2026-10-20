package johnsmith.enchantingoverhauled.menu.enchant.controller;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantContext;
import johnsmith.enchantingoverhauled.api.config.type.UpgradePriorityType;
import johnsmith.enchantingoverhauled.api.config.type.UpgradeSourceType;

import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class EnchantActionFactory {

    public static EnchantAction createFrom(EnchantContext context) {
        Constants.LOG.debug("EnchantActionFactory: Evaluating Context for Slot {}", context.slotIndex());

        return context.pending().enchantment()
                .map(pending -> evaluateAction(context, pending))
                .orElseGet(EnchantAction.None::new);
    }

    private static EnchantAction evaluateAction(EnchantContext context, EnchantmentInstance pending) {
        Constants.LOG.debug(" - Initial Source Evaluation: {}", context.pending().source());

        return switch (context.pending().source()) {
            case TABLE -> new EnchantAction.Apply(pending, context.xpCost(), context.lapisCost(), context.slotIndex());
            case TARGET -> evaluateTargetUpgrade(context, pending);
            case SOURCE -> resolveConflict(context, pending);
            default -> new EnchantAction.None();
        };
    }

    private static EnchantAction evaluateTargetUpgrade(EnchantContext context, EnchantmentInstance pending) {
        int targetLevel = context.applied().map(e -> e.level).orElse(0);
        int maxLevel = pending.enchantment.value().getMaxLevel();
        UpgradeSourceType sourceType = Config.UPGRADE_SOURCE_TYPE.get();

        // Pure upgrades (without a catalyst) are exclusively permitted if configured to NONE
        // AND strictly bounded to the enchantment's max level
        if (sourceType == UpgradeSourceType.NONE && targetLevel < maxLevel) {
            return new EnchantAction.Upgrade(pending, targetLevel, context.xpCost(), context.lapisCost(), context.slotIndex(), false);
        }

        return new EnchantAction.None();
    }

    private static EnchantAction resolveConflict(EnchantContext context, EnchantmentInstance pending) {
        int targetLevel = context.applied().map(e -> e.level).orElse(0);
        int sourceLevel = pending.level;
        int maxLevel = pending.enchantment.value().getMaxLevel();

        // An enchantment completely absent from the target strictly mandates a transfer
        if (targetLevel == 0) {
            return new EnchantAction.Transfer(pending, context.xpCost(), context.lapisCost(), context.slotIndex());
        }

        Constants.LOG.debug(" - Conflict Resolution Triggered. Target Lvl: {}, Source Lvl: {}, Max Lvl: {}", targetLevel, sourceLevel, maxLevel);

        // Strict overleveling logic
        if (targetLevel >= maxLevel) {
            // Overleveling is exclusively permitted ONCE and strictly requires a GREATER source
            if (targetLevel == maxLevel && sourceLevel > targetLevel) {
                EnchantmentInstance overleveledInstance = new EnchantmentInstance(pending.enchantment, maxLevel + 1);
                return new EnchantAction.Transfer(overleveledInstance, context.xpCost(), context.lapisCost(), context.slotIndex());
            }
            return new EnchantAction.None();
        }

        UpgradeSourceType sourceType = Config.UPGRADE_SOURCE_TYPE.get();
        UpgradePriorityType priorityType = Config.UPGRADE_PRIORITY_TYPE.get();

        boolean canUpgrade = isValidUpgrade(targetLevel, sourceLevel, sourceType);
        boolean canTransfer = sourceLevel > targetLevel;

        // Only consume the source if the upgrade validation strictly relied upon it
        boolean usesSource = sourceType != UpgradeSourceType.NONE;

        Constants.LOG.debug(" - Evaluation - canUpgrade: {}, canTransfer: {}", canUpgrade, canTransfer);

        if (canUpgrade && canTransfer) {
            Constants.LOG.debug(" - Conflict Detected. Resolving via Priority: {}", priorityType);
            return switch (priorityType) {
                case TRANSFER -> new EnchantAction.Transfer(pending, context.xpCost(), context.lapisCost(), context.slotIndex());
                case UPGRADE -> new EnchantAction.Upgrade(pending, targetLevel, context.xpCost(), context.lapisCost(), context.slotIndex(), usesSource);
            };
        }

        if (canUpgrade) {
            return new EnchantAction.Upgrade(pending, targetLevel, context.xpCost(), context.lapisCost(), context.slotIndex(), usesSource);
        }

        if (canTransfer) {
            return new EnchantAction.Transfer(pending, context.xpCost(), context.lapisCost(), context.slotIndex());
        }

        Constants.LOG.debug(" - No valid action found. Locking slot.");
        return new EnchantAction.None();
    }

    private static boolean isValidUpgrade(int targetLevel, int sourceLevel, UpgradeSourceType type) {
        return switch (type) {
            case NONE -> true;
            case EQUAL -> sourceLevel >= targetLevel;
            case GREATER -> sourceLevel > targetLevel;
        };
    }
}