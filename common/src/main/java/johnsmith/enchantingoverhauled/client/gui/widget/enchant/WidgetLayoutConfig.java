package johnsmith.enchantingoverhauled.client.gui.widget.enchant;

/**
 * A configuration record encapsulating all static positioning and dimensional constants
 * for the structural rendering of an {@link EnchantWidget}.
 * <p>
 * Extracted to avoid magic numbers and facilitate centralized UI scaling adjustments.
 *
 * @param buttonWidth   The overall width of the enchantment slot background button.
 * @param buttonHeight  The overall height of the enchantment slot background button.
 * @param costWidth     The width of the lapis cost sprite indicator.
 * @param costHeight    The height of the lapis cost sprite indicator.
 * @param costXOffset   The internal X offset relative to the button origin for the cost sprite.
 * @param costYOffset   The internal Y offset relative to the button origin for the cost sprite.
 * @param powerXOffset  The internal X offset for the required XP power text.
 * @param powerYOffset  The internal Y offset for the required XP power text.
 * @param textXOffset   The internal X offset for the enchantment's name component.
 * @param textYOffset   The internal Y offset for the enchantment's name component.
 * @param textMaxWidth  The maximum pixel width allowed before the text wraps/truncates.
 */
public record WidgetLayoutConfig(
        int buttonWidth,
        int buttonHeight,
        int costWidth,
        int costHeight,
        int costXOffset,
        int costYOffset,
        int powerXOffset,
        int powerYOffset,
        int textXOffset,
        int textYOffset,
        int textMaxWidth
) {
    /**
     * The standard default layout definitions matching the vanilla enchanting UI proportions.
     */
    public static final WidgetLayoutConfig DEFAULT = new WidgetLayoutConfig(
            115, 18, 16, 16, 0, 2, 97, 8, 16, 5, 89
    );
}