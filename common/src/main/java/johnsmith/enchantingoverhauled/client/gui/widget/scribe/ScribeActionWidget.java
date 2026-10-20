package johnsmith.enchantingoverhauled.client.gui.widget.scribe;

import com.mojang.blaze3d.systems.RenderSystem;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGameState;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGridData;
import johnsmith.enchantingoverhauled.menu.scribe.controller.ScribeInput;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Handles the interaction, validation, and rendering of the primary UI button
 * responsible for starting or aborting the Scribe Minigame process.
 */
public class ScribeActionWidget {

    protected final ScribeGridData gridData;
    protected final Supplier<Integer> lapisCount;
    protected final Supplier<Boolean> isCreative;
    protected final ScribeActionWidgetTheme theme;

    protected int leftPos, topPos;
    protected boolean isHovered;

    public ScribeActionWidget(ScribeGridData gridData, Supplier<Integer> lapisCount, Supplier<Boolean> isCreative, ScribeActionWidgetTheme theme) {
        this.gridData = gridData;
        this.lapisCount = lapisCount;
        this.isCreative = isCreative;
        this.theme = theme;
    }

    public void updatePosition(int leftPos, int topPos) {
        this.leftPos = leftPos;
        this.topPos = topPos;
    }

    protected boolean isAffordable() {
        if (this.theme.actionPayload() != ScribeInput.ACTION_START) return true;
        if (this.gridData.getGameState() != ScribeGameState.READY) return false;
        if (this.isCreative.get()) return true;

        return this.lapisCount.get() >= this.gridData.getLapisCost();
    }

    public int mouseClicked(double mouseX, double mouseY) {
        if (this.isHovered && this.isAffordable()) {
            return new ScribeInput(0, 0, this.theme.actionPayload()).encode();
        }
        return -1;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, Font font) {
        int x = this.leftPos + 48;
        int y = this.topPos + 108;
        int width = 120;
        int height = 16;

        this.isHovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        boolean affordable = this.isAffordable();

        ResourceLocation texture = affordable ? (this.isHovered ? ScribeActionWidgetTheme.TEX_HIGHLIGHTED : ScribeActionWidgetTheme.TEX_ENABLED) : ScribeActionWidgetTheme.TEX_DISABLED;

        RenderSystem.enableBlend();
        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();

        Component text = this.theme.textProvider().apply(this.gridData);

        // Define uniform text colors dynamically based on the current rendering state
        int textColor = affordable ? (this.isHovered ? 0xFF000000 : 0xFF555555) : 0xFFFFFFFF;
        int textX = x + (width / 2) - (font.width(text) / 2);

        // Render text without a drop shadow
        guiGraphics.drawString(font, text, textX, y + 4, textColor, false);
    }

    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, Font font) {
        if (this.isHovered && this.theme.actionPayload() == ScribeInput.ACTION_START) {
            List<FormattedCharSequence> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("gui.enchanting_overhauled.scribe.start_cost").withStyle(ChatFormatting.WHITE).getVisualOrderText());

            tooltip.add(FormattedCharSequence.EMPTY);

            int cost = this.gridData.getLapisCost();
            boolean canAfford = this.isCreative.get() || this.lapisCount.get() >= cost;

            MutableComponent lapisText = cost == 1 ? Component.translatable("container.enchant.lapis.one") : Component.translatable("container.enchant.lapis.many", cost);
            tooltip.add(lapisText.withStyle(canAfford ? ChatFormatting.GRAY : ChatFormatting.RED).getVisualOrderText());

            guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
        }
    }
}