package johnsmith.enchantingoverhauled.menu.enchant.controller;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantContext;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantRenderState;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantState;

import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.Optional;

/**
 * A factory utility that translates pure functional backend {@link EnchantContext} records
 * into purely visual frontend {@link EnchantRenderState} records for the UI to consume.
 * <p>
 * Decouples the UI widgets from needing to understand enchantment conflict resolution
 * logic, isolating the view from the model.
 */
public class EnchantRenderStateFactory {

    // region Factory Generation
    /**
     * Evaluates a backend context and transforms it into a visual render state.
     *
     * @param context The data context representing an active slot.
     * @return The built render state.
     */
    public static EnchantRenderState createFrom(EnchantContext context) {
        Constants.LOG.debug("EnchantmentRenderStateFactory: Building Render State for Context Slot {}", context.slotIndex());

        return context.pending().enchantment()
                .map(pending -> buildState(context, pending))
                .orElseGet(EnchantRenderStateFactory::buildEmptyState);
    }

    private static EnchantRenderState buildState(EnchantContext context, EnchantmentInstance pending) {
        EnchantAction action = EnchantActionFactory.createFrom(context);

        int currentLevel = context.applied().map(e -> e.level).orElse(0);
        int maxLevel = pending.enchantment.value().getMaxLevel();

        EnchantmentSource visualSource = context.pending().source();

        // Map the resolved functional action back to the appropriate visual classification
        if (action instanceof EnchantAction.Upgrade) {
            visualSource = EnchantmentSource.TARGET;
        } else if (action instanceof EnchantAction.Transfer) {
            visualSource = EnchantmentSource.SOURCE;
        } else if (action instanceof EnchantAction.Apply) {
            visualSource = EnchantmentSource.TABLE;
        }

        int displayLevel = currentLevel > 0 ? currentLevel : pending.level;

        EnchantState state = evaluateState(action, currentLevel, maxLevel);

        EnchantRenderState finalState = new EnchantRenderState(
                visualSource,
                Optional.of(pending.enchantment),
                displayLevel,
                context.xpRequirement(),
                context.xpCost(),
                context.lapisCost(),
                state
        );

        Constants.LOG.debug(" - Generated State: {}", finalState);
        return finalState;
    }
    // endregion

    // region Evaluation Helpers
    /**
     * Classifies the structural interactivity of the widget based on the functional action generated.
     * Determines whether the slot is a standard clickable action, or blocked due to being Maxed/Missing Catalysts.
     */
    private static EnchantState evaluateState(EnchantAction action, int currentLevel, int maxLevel) {
        // If an action is locked but maxed, display the corresponding max label over the padlock
        if (action instanceof EnchantAction.None) {
            if (currentLevel > maxLevel) {
                return EnchantState.OVERLEVELED;
            }
            if (currentLevel == maxLevel) {
                return EnchantState.MAXED;
            }
            return EnchantState.LOCKED;
        }
        return EnchantState.AVAILABLE;
    }

    /**
     * Returns a blank placeholder state representing a visually empty slot with no valid
     * target or available options.
     */
    public static EnchantRenderState buildEmptyState() {
        return new EnchantRenderState(
                EnchantmentSource.NONE,
                Optional.empty(),
                0,
                0,
                0,
                0,
                EnchantState.EMPTY
        );
    }
    // endregion
}