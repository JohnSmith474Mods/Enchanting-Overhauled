package johnsmith.enchantingoverhauled.client.gui.tooltip.enchant;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * A data record encapsulating all state dependencies required to build and render
 * an {@link EnchantTooltip} command successfully.
 *
 * @param font             The client's active font renderer.
 * @param lapisCount       The amount of Lapis Lazuli currently available in the fuel slot.
 * @param isCreative       True if the current player is in creative mode, skipping resource costs.
 * @param enchantment      The specific enchantment being evaluated for display.
 * @param level            The resolved display level (either the target's current level, or the incoming pending level).
 * @param powerRequirement The total enchanting power (bookshelves) required to unlock this slot.
 * @param cost             The base experience level and lapis cost to execute the action.
 * @param minecraft        The active Minecraft client instance for accessing world registry references.
 */
public record EnchantTooltipContext(
        Font font,
        int lapisCount,
        boolean isCreative,
        Holder<Enchantment> enchantment,
        int level,
        int powerRequirement,
        int cost,
        Minecraft minecraft
) {}