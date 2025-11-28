package johnsmith.enchantingoverhauled.api.config.client.gui.entry;

import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.client.gui.ConfigList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * A configuration entry implementation for single-precision floating point properties.
 * <p>
 * This entry renders an {@link EditBox} that allows users to input decimal numbers
 * (up to two decimal places for display). It enforces bounds validation and provides
 * visual feedback for invalid input.
 */
public class FloatEntry extends OptionEntry<Float, EditBox> {

    /**
     * Constructs a new float configuration entry.
     *
     * @param type           The bounded float property backing this entry.
     * @param minecraft      The Minecraft client instance.
     * @param parentList     The parent configuration list containing this entry.
     * @param onValueChanged A callback runnable to execute when the value is modified by the user.
     */
    public FloatEntry(Property.Bounded<Float> type, Minecraft minecraft, ConfigList parentList, Runnable onValueChanged) {
        super(type, minecraft, parentList, onValueChanged);
        this.widget.setValue(String.format("%.2f", type.get()));
    }

    /**
     * Creates and configures the edit box widget for this entry.
     * <p>
     * The widget is configured to accept numeric input. The responder logic validates
     * that the parsed float value falls between the defined lower and upper bounds.
     * If validation fails, the text color is set to red (0xFF0000); otherwise, it is white (0xFFFFFF).
     *
     * @return A new {@link EditBox} instance.
     */
    @Override
    protected EditBox createWidget() {
        EditBox box = new EditBox(minecraft.font, 0, 0, 75, 20, Component.empty());
        box.setFilter(s -> s.matches("-?\\d*\\.?\\d*"));
        box.setResponder(s -> {
            try {
                if (s.isEmpty() || s.equals("-") || s.equals(".")) return;
                float val = Float.parseFloat(s);
                Property.Bounded<Float> bounds = (Property.Bounded<Float>) configType;

                if (val >= bounds.lowerBound && val <= bounds.upperBound) {
                    configType.set(val);
                    box.setTextColor(0xFFFFFFFF);
                } else {
                    box.setTextColor(0xFFFF0000);
                }
                updateResetButton();
                this.onValueChanged.run();
            } catch (NumberFormatException ignored) {
                box.setTextColor(0xFFFF0000);
            }
        });
        return box;
    }

    /**
     * Synchronizes the widget's text with the underlying property value.
     * <p>
     * The value is formatted to two decimal places and the text color is reset to white.
     */
    @Override
    protected void updateWidgetValue() {
        this.widget.setValue(String.format("%.2f", configType.get()));
        this.widget.setTextColor(0xFFFFFFFF);
    }

    /**
     * Checks if the current value is equivalent to the default value.
     * <p>
     * Uses a small tolerance (`0.0001f`) for floating-point comparison to account
     * for precision errors.
     *
     * @return True if the current value is approximately equal to the default value.
     */
    @Override
    public boolean isDefault() {
        return Math.abs(configType.get() - configType.defaultValue) < 0.0001f;
    }
}