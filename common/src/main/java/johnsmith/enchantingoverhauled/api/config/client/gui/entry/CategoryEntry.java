package johnsmith.enchantingoverhauled.api.config.client.gui.entry;

import johnsmith.enchantingoverhauled.api.config.data.PropertyGroup;

import com.google.common.collect.ImmutableList;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * A configuration list entry that serves as a visual section header.
 * <p>
 * This entry displays a centered, bold, yellow title corresponding to a {@link PropertyGroup}.
 * It is non-interactive and purely for organizational purposes within the configuration screen.
 */
public class CategoryEntry extends Entry {
    private final Minecraft minecraft;
    private final Component label;
    private final int textWidth;

    /**
     * Constructs a new category header entry.
     *
     * @param category  The property group this header represents.
     * @param minecraft The Minecraft client instance used for font rendering.
     */
    public CategoryEntry(PropertyGroup category, Minecraft minecraft) {
        this.minecraft = minecraft;
        this.label = Component.translatable(category.translationKey())
                .withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
        this.textWidth = minecraft.font.width(this.label);
    }

    /**
     * Renders the category label centered horizontally on the screen.
     *
     * @param guiGraphics The graphics context for rendering.
     * @param index       The index of this entry in the list.
     * @param top         The y-coordinate of the top of the entry.
     * @param left        The x-coordinate of the left of the entry.
     * @param width       The width of the entry.
     * @param height      The height of the entry.
     * @param mouseX      The current mouse x-coordinate.
     * @param mouseY      The current mouse y-coordinate.
     * @param hovering    Whether the mouse is hovering over this entry.
     * @param partialTick The partial tick time.
     */
    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
        int textY = top + (height - minecraft.font.lineHeight) / 2;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        guiGraphics.drawString(minecraft.font, this.label, screenWidth / 2 - this.textWidth / 2, textY, 0xFFFFFF, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns an empty list as this entry has no interactive child widgets.
     */
    @Override
    public @NotNull List<? extends GuiEventListener> children() { return Collections.emptyList(); }

    /**
     * {@inheritDoc}
     * <p>
     * Provides a narratable element for the category title to support screen readers.
     */
    @Override
    public @NotNull List<? extends NarratableEntry> narratables() {
        return ImmutableList.of(new NarratableEntry() {
            @Override public @NotNull NarrationPriority narrationPriority() { return NarrationPriority.HOVERED; }
            @Override public void updateNarration(@NotNull NarrationElementOutput output) { output.add(NarratedElementType.TITLE, label); }
        });
    }
}