package johnsmith.enchantingoverhauled.menu.enchant.model;

import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Optional;

/**
 * An immutable data record representing the evaluated visual front-end state
 * of an enchantment slot widget.
 * <p>
 * This object decouples the pure logic representation ({@link EnchantContext})
 * from the GUI elements, pre-calculating the exact display text levels, textures,
 * and interactivity statuses needed by the widgets.
 *
 * @param visualSource  The origin classification (Target, Source, Table) dictating the widget's texture and color palette.
 * @param enchantment   The target enchantment to be displayed.
 * @param displayLevel  The specific level integer to format and display in the UI (e.g., "II" or "V").
 * @param xpRequirement The enchanting power required to display the slot as active.
 * @param xpCost        The experience cost to execute the action, determining the level sprite shown.
 * @param lapisCost     The lapis cost to execute the action.
 * @param state         The structural interactivity state of the widget (e.g., AVAILABLE, MAXED, LOCKED).
 */
public record EnchantRenderState(
        EnchantmentSource visualSource,
        Optional<Holder<Enchantment>> enchantment,
        int displayLevel,
        int xpRequirement,
        int xpCost,
        int lapisCost,
        EnchantState state
) {
    // region Overrides
    /**
     * Returns a string representation of this render state, formatting the inner holders
     * for easier debugging and layout tracking.
     *
     * @return The formatted string.
     */
    @Override
    public String toString() {
        String enchStr = enchantment.map(e -> e.unwrapKey().map(k -> k.location().toString()).orElse("unknown")).orElse("empty");
        return "EnchantRenderState[source=" + visualSource + ", enchantment=" + enchStr + ", lvl=" + displayLevel + ", xpReq=" + xpRequirement + ", xpCost=" + xpCost + ", lapis=" + lapisCost + ", state=" + state + "]";
    }
    // endregion
}