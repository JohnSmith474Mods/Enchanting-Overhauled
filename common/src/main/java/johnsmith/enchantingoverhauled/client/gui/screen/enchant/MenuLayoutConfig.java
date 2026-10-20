package johnsmith.enchantingoverhauled.client.gui.screen.enchant;

/**
 * A configuration record encapsulating all static positioning, dimensions, and offset logic
 * for the components inside the Enchantment UI Screen.
 *
 * @param screenHeightAdjustment  The amount to increase the GUI's total height by.
 * @param rerollButtonXOffset     The X relative offset of the reroll button.
 * @param rerollButtonYOffset     The Y relative offset of the reroll button.
 * @param rerollButtonWidth       The standard width of the reroll button.
 * @param rerollButtonHeight      The standard height of the reroll button.
 * @param rerollCostWidth         The width of the reroll's lapis cost overlay sprite.
 * @param rerollCostHeight        The height of the reroll's lapis cost overlay sprite.
 * @param rerollCostXOffset       The relative X offset for the lapis cost sprite.
 * @param rerollCostYOffset       The relative Y offset for the lapis cost sprite.
 * @param enchantingButtonXOffset The starting X offset for the enchantment list widgets.
 * @param enchantingButtonYOffset The starting Y offset for the first enchantment widget.
 * @param enchantingButtonHeight  The height of a single enchantment widget row.
 * @param pageSize                The total number of enchantment widgets visible at one time.
 * @param backwardButtonWidth     The width of the previous page navigation arrow.
 * @param backwardButtonHeight    The height of the previous page navigation arrow.
 * @param forwardButtonWidth      The width of the next page navigation arrow.
 * @param forwardButtonHeight     The height of the next page navigation arrow.
 * @param backwardXOffset         The relative X offset of the previous page button.
 * @param backwardYOffset         The relative Y offset of the previous page button.
 * @param forwardXOffset          The relative X offset of the next page button.
 * @param forwardYOffset          The relative Y offset of the next page button.
 * @param experienceBarXOffset    The relative X offset for drawing the custom XP bar.
 * @param experienceBarYOffset    The relative Y offset for drawing the custom XP bar.
 * @param experienceBarWidth      The total visual width of the custom XP bar.
 * @param experienceBarHeight     The visual height of the custom XP bar.
 */
public record MenuLayoutConfig(
        int screenHeightAdjustment,
        int rerollButtonXOffset,
        int rerollButtonYOffset,
        int rerollButtonWidth,
        int rerollButtonHeight,
        int rerollCostWidth,
        int rerollCostHeight,
        int rerollCostXOffset,
        int rerollCostYOffset,
        int enchantingButtonXOffset,
        int enchantingButtonYOffset,
        int enchantingButtonHeight,
        int pageSize,
        int backwardButtonWidth,
        int backwardButtonHeight,
        int forwardButtonWidth,
        int forwardButtonHeight,
        int backwardXOffset,
        int backwardYOffset,
        int forwardXOffset,
        int forwardYOffset,
        int experienceBarXOffset,
        int experienceBarYOffset,
        int experienceBarWidth,
        int experienceBarHeight
) {
    /**
     * The standard default layout definitions mirroring the 1.21 vanilla UI configuration,
     * adjusted to fit the expanded slots.
     */
    public static final MenuLayoutConfig DEFAULT = new MenuLayoutConfig(
            29, 8, 17, 36, 18, 16, 16, -1, 1, 52, 19, 18, 3, 9, 10, 10, 10, 100, 5, 109, 5, 59, 85, 102, 5
    );
}