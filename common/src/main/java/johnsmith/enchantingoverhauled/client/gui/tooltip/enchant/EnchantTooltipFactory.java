package johnsmith.enchantingoverhauled.client.gui.tooltip.enchant;

import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantRenderState;

/**
 * A factory utility class for evaluating visual render states and instantiating
 * the corresponding {@link EnchantTooltip} command implementation.
 */
public class EnchantTooltipFactory {

    /**
     * Evaluates the provided widget state and context to return the correct Tooltip command.
     * <p>
     * Order of operations prioritizes structural states (Maxed, Locked) before evaluating
     * action-based source typings (Target upgrade, Source transfer, Table apply).
     *
     * @param state The current UI render state for the slot.
     * @param ctx   The data context for the tooltip instantiation.
     * @return A constructed instance implementing {@link EnchantTooltip}.
     */
    public static EnchantTooltip create(EnchantRenderState state, EnchantTooltipContext ctx) {
        switch (state.state()) {
            case MAXED, OVERLEVELED -> { return new EnchantTooltip.Maxed(ctx); }
            case LOCKED -> { return new EnchantTooltip.MissingSource(ctx); }
            default -> {}
        }

        return switch (state.visualSource()) {
            case TARGET -> new EnchantTooltip.Upgrade(ctx);
            case SOURCE -> new EnchantTooltip.Transfer(ctx);
            case TABLE -> new EnchantTooltip.Apply(ctx);
            case NONE -> new EnchantTooltip.Empty();
        };
    }
}