package johnsmith.enchantingoverhauled.client.gui.tooltip.scribe;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.client.UIFormattingUtil;
import johnsmith.enchantingoverhauled.api.config.type.ColoringMode;
import johnsmith.enchantingoverhauled.api.config.type.FormatApplicationMode;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the generation and static positioning of the Scribe Minigame tooltips.
 */
public class ScribeTooltipProvider {

    /**
     * Renders the tooltip for a hovered enchantment.
     */
    public static void renderTooltip(GuiGraphics guiGraphics, Font font, ScribeTooltipContext ctx, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();

        FormatApplicationMode mode = Config.SCRIBE_MENU_FORMAT_APPLICATION_MODE.get();
        ColoringMode themeColoring = (mode == FormatApplicationMode.TOOLTIP || mode == FormatApplicationMode.ALL) ? Config.SCRIBE_MENU_THEME_COLORING.get() : ColoringMode.NONE;
        ColoringMode levelColoring = (mode == FormatApplicationMode.TOOLTIP || mode == FormatApplicationMode.ALL) ? Config.SCRIBE_MENU_LEVEL_COLORING.get() : ColoringMode.NONE;
        boolean showFirstLevel = (mode == FormatApplicationMode.TOOLTIP || mode == FormatApplicationMode.ALL) && Config.SCRIBE_MENU_SHOW_FIRST_LEVEL.get();

        tooltip.add(UIFormattingUtil.formatEnchantmentName(ctx.ench(), ctx.level(), ctx.obfuscate(), themeColoring, levelColoring, showFirstLevel).getVisualOrderText());
        tooltip.addAll(UIFormattingUtil.formatEnchantmentDescription(ctx.ench(), ctx.level(), ctx.obfuscate(), themeColoring, levelColoring).stream().map(Component::getVisualOrderText).toList());

        ClientTooltipPositioner positioner = new ClientTooltipPositioner() {
            @Override
            public @NotNull Vector2ic positionTooltip(int screenWidth, int screenHeight, int mX, int mY, int tooltipWidth, int tooltipHeight) {
                int x;
                if (ctx.isSource()) {
                    // Tooltip covering the Index page (aligned to the right edge of the Source page)
                    x = ctx.anchorX() + 5;
                } else {
                    // Tooltip covering the Source page (aligned to the left edge of the Index page)
                    x = ctx.anchorX() - tooltipWidth - 5;
                }

                int y = ctx.itemY();
                if (y + tooltipHeight > screenHeight) {
                    y = screenHeight - tooltipHeight - 2;
                }
                return new Vector2i(x, y);
            }
        };

        guiGraphics.renderTooltip(font, tooltip, positioner, mouseX, mouseY);
    }
}