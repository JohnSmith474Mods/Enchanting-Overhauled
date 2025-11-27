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

    protected final Minecraft minecraft;
    protected final ConfigList parentList;
    protected final Runnable onValueChanged;

    /**
     * Constructs an OptionEntry, initializing the label, reset button, and main widget.
     *
     * @param configType     The data-driven property instance managed by this entry.
     * @param minecraft      The Minecraft client instance.
     * @param parentList     The parent configuration list, used primarily to access scroll position for layout.
     * @param onValueChanged A callback to execute after the property value is successfully changed,
     * used to update the parent screen.
     */
    public OptionEntry(Property<T> configType, Minecraft minecraft, ConfigList parentList, Runnable onValueChanged) {
        this.configType = configType;
        this.minecraft = minecraft;
        this.parentList = parentList;
        this.onValueChanged = onValueChanged;

        this.labelComponent = Component.translatable(configType.translationKey);
        this.resetButton = Button.builder(Component.translatable("controls.reset"), b -> reset())
                .bounds(0, 0, 50, 20).build();
        this.widget = createWidget();
        updateResetButton();
    }

    /**
     * Abstract method responsible for creating the specific interactive widget.
     * Subclasses must implement this to define how the property value is edited.
     *
     * @return A configured instance of the main interactive widget type {@code W}.
     */
    protected abstract W createWidget();

    /**
     * Abstract method responsible for synchronizing the widget's visual state
     * with the current value of {@code configType}.
     * This is called after a load or reset operation.
     */
    protected abstract void updateWidgetValue();

    /**
     * Resets the configuration property back to its {@code defaultValue}.
     * Updates the widget, refreshes the reset button state, and triggers the change callback.
     */
    public void reset() {
        configType.set(configType.defaultValue);
        updateWidgetValue();
        updateResetButton();
        this.onValueChanged.run();
    }

    /**
     * Checks if the current value of the property is equal to its default value.
     *
     * @return True if the current and default values are equal (using {@link Objects#equals}).
     */
    public boolean isDefault() {
        return Objects.equals(configType.get(), configType.defaultValue);
    }

    /**
     * Updates the active state of the {@code resetButton} based on the result of {@link #isDefault()}.
     */
    protected void updateResetButton() {
        this.resetButton.active = !isDefault();
    }

    /**
     * Renders the entry, including the localized label, the input widget, and the reset button.
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
        guiGraphics.drawString(minecraft.font, this.labelComponent, left, textY, 0xFFFFFF);
    }

    /**
     * {@inheritDoc}
     * Returns an immutable list containing the main widget and the reset button as interactive children.
     */
    @Override
    public @NotNull List<? extends GuiEventListener> children() { return ImmutableList.of(widget, resetButton); }

    /**
     * {@inheritDoc}
     * Returns an immutable list containing the main widget and the reset button for narration support.
     */
    @Override
    public @NotNull List<? extends NarratableEntry> narratables() { return ImmutableList.of(widget, resetButton); }
}