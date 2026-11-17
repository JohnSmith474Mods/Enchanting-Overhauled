package johnsmith.enchantingoverhauled.api.config.client.gui.entry;

import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.client.gui.ConfigList;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * An abstract base class for all interactive configuration screen entries.
 * <p>
 * This class handles common functionality for displaying a configurable property
 * (e.g., integer, boolean) including the label, the interactive widget, and a
 * dedicated "Reset to Default" button.
 *
 * @param <T> The data type of the configuration property (e.g., {@code Integer}, {@code Float}).
 * @param <W> The type of the main interactive widget (e.g., {@code EditBox}, {@code CycleButton}).
 */
public abstract class OptionEntry<T extends Comparable<T>, W extends AbstractWidget> extends Entry {
    protected final Property<T> configType;
    protected final Button resetButton;
    protected final W widget;
    protected final Component labelComponent;

    // NEW: Cache the tooltip to avoid performance hits during render
    protected final List<FormattedCharSequence> tooltip;

    protected final Minecraft minecraft;
    protected final ConfigList parentList;
    protected final Runnable onValueChanged;

    public OptionEntry(Property<T> configType, Minecraft minecraft, ConfigList parentList, Runnable onValueChanged) {
        this.configType = configType;
        this.minecraft = minecraft;
        this.parentList = parentList;
        this.onValueChanged = onValueChanged;

        this.labelComponent = Component.translatable(configType.translationKey);

        this.tooltip = minecraft.font.split(configType.getTranslatedDescription(), 200);

        this.resetButton = Button.builder(Component.translatable("controls.reset"), b -> reset())
                .bounds(0, 0, 50, 20).build();
        this.widget = createWidget();
        updateResetButton();
    }

    protected abstract W createWidget();

    protected abstract void updateWidgetValue();

    public void reset() {
        configType.set(configType.defaultValue);
        updateWidgetValue();
        updateResetButton();
        this.onValueChanged.run();
    }

    public boolean isDefault() {
        return Objects.equals(configType.get(), configType.defaultValue);
    }

    protected void updateResetButton() {
        this.resetButton.active = !isDefault();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
        int y = top + (height - 20) / 2;

        int resetX = this.parentList.getScrollbarX() - 50 - 10;

        this.resetButton.setPosition(resetX, y);
        this.resetButton.render(guiGraphics, mouseX, mouseY, partialTick);

        int widgetWidth = 75;
        int widgetX = resetX - 5 - widgetWidth;
        this.widget.setX(widgetX);
        this.widget.setY(y);
        this.widget.setWidth(widgetWidth);
        this.widget.render(guiGraphics, mouseX, mouseY, partialTick);

        int textY = top + (height - minecraft.font.lineHeight) / 2;

        // Truncate label if it overlaps the widget
        int maxLabelWidth = widgetX - left - 5;
        if (maxLabelWidth > 0) {
            // Draws text with handling for overflow
            guiGraphics.drawString(minecraft.font, this.labelComponent, left, textY, 0xFFFFFF);
        }

        if (hovering) {
            guiGraphics.renderTooltip(minecraft.font, this.tooltip, mouseX, mouseY);
        }
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() { return ImmutableList.of(widget, resetButton); }

    @Override
    public @NotNull List<? extends NarratableEntry> narratables() { return ImmutableList.of(widget, resetButton); }
}