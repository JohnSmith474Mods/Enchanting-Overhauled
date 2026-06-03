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
    public void renderContent(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
        // 1. Calculate Y position for centering widgets vertically in this row
        //    'this.getY()' is the top Y coordinate of this entry row.
        int y = this.getY() + (this.getHeight() - 20) / 2;

        // 2. Calculate X positions relative to the scrollbar
        //    (You might need to adjust getScrollbarX() in ConfigList to be compatible with 1.21.10 layout if it changed,
        //     but standard logic is often getRowRight() or similar)
        int resetX = this.parentList.getScrollbarX() - 50 - 10;

        // 3. Position and Render Reset Button
        this.resetButton.setX(resetX);
        this.resetButton.setY(y);
        this.resetButton.render(guiGraphics, mouseX, mouseY, partialTick);

        // 4. Position and Render Main Widget (your 'myButton' equivalent)
        int widgetWidth = 75;
        int widgetX = resetX - 5 - widgetWidth;

        this.widget.setX(widgetX);
        this.widget.setY(y);
        this.widget.setWidth(widgetWidth);
        this.widget.render(guiGraphics, mouseX, mouseY, partialTick);

        // 5. Render Label Text
        //    'this.getX()' is the left X coordinate of this entry row.
        int textY = this.getY() + (this.getHeight() - minecraft.font.lineHeight) / 2;

        // Truncate label if it overlaps the widget
        int maxLabelWidth = widgetX - this.getX() - 5;
        if (maxLabelWidth > 0) {
            // Draws text with handling for overflow
            guiGraphics.drawString(minecraft.font, this.labelComponent, this.getX(), textY, 0xFFFFFFFF);
        }

        if (isHovering) {
            guiGraphics.setTooltipForNextFrame(minecraft.font, this.tooltip, mouseX, mouseY);
        }
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() { return ImmutableList.of(widget, resetButton); }

    @Override
    public @NotNull List<? extends NarratableEntry> narratables() { return ImmutableList.of(widget, resetButton); }
}