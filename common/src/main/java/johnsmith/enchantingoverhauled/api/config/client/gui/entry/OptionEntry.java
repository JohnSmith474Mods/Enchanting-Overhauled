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

public abstract class OptionEntry<T extends Comparable<T>, W extends AbstractWidget> extends Entry {
    protected final Property<T> configType;
    protected final Button resetButton;
    protected final W widget;
    protected final Component labelComponent;

    // Dependencies that must be passed in now
    protected final Minecraft minecraft;
    protected final ConfigList parentList;
    protected final Runnable onValueChanged;

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

    protected abstract W createWidget();
    protected abstract void updateWidgetValue();

    public void reset() {
        configType.set(configType.defaultValue);
        updateWidgetValue();
        updateResetButton();
        this.onValueChanged.run(); // Triggers screen update via callback
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

        // Requires ConfigList to have a public int getScrollbarX() method
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

    @Override
    public @NotNull List<? extends GuiEventListener> children() { return ImmutableList.of(widget, resetButton); }
    @Override
    public @NotNull List<? extends NarratableEntry> narratables() { return ImmutableList.of(widget, resetButton); }
}