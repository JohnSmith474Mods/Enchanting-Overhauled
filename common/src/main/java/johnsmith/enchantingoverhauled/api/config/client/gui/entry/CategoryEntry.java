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

public class CategoryEntry extends Entry {
    private final Minecraft minecraft;
    private final Component label;
    private final int textWidth;

    public CategoryEntry(PropertyGroup category, Minecraft minecraft) {
        this.minecraft = minecraft;
        this.label = Component.translatable(category.translationKey())
                .withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
        this.textWidth = minecraft.font.width(this.label);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
        int textY = top + (height - minecraft.font.lineHeight) / 2;

        // Center on the screen
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        guiGraphics.drawString(minecraft.font, this.label, screenWidth / 2 - this.textWidth / 2, textY, 0xFFFFFF, false);
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() { return Collections.emptyList(); }

    @Override
    public @NotNull List<? extends NarratableEntry> narratables() {
        return ImmutableList.of(new NarratableEntry() {
            @Override public @NotNull NarrationPriority narrationPriority() { return NarrationPriority.HOVERED; }
            @Override public void updateNarration(@NotNull NarrationElementOutput output) { output.add(NarratedElementType.TITLE, label); }
        });
    }
}