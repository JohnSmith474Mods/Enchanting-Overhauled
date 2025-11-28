package johnsmith.enchantingoverhauled.api.config.client.gui.entry;

import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.client.gui.ConfigList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * A configuration entry implementation for double-precision floating point properties.
 * <p>
 * This entry renders an {@link EditBox} that allows users to input decimal numbers.
 * It includes input validation to ensure the value remains within the defined bounds
 * (min/max) and provides visual feedback (red text) for invalid inputs.
 */
public class DoubleEntry extends OptionEntry<Double, EditBox> {

    /**
     * Constructs a new double configuration entry.
     *
     * @param type           The bounded double property backing this entry.
     * @param minecraft      The Minecraft client instance.
     * @param parentList     The parent configuration list containing this entry.
     * @param onValueChanged A callback runnable to execute when the value is modified by the user.
     */
    public DoubleEntry(Property.Bounded<Double> type, Minecraft minecraft, ConfigList parentList, Runnable onValueChanged) {
        super(type, minecraft, parentList, onValueChanged);
        this.widget.setValue(String.format("%.2f", type.get()));
    }

    /**
     * Creates and configures the edit box widget for this entry.
     * <p>
     * The widget is configured with a filter to only accept valid numeric characters
     * (digits, negative sign, decimal point). The responder listener handles parsing
     * the input string, checking if it lies within the property's bounds, and updating
     * the text color to red if invalid or white if valid.
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
                double val = Double.parseDouble(s);
                Property.Bounded<Double> bounds = (Property.Bounded<Double>) configType;

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
     * The value is formatted to two decimal places. This method also resets the
     * text color to white, assuming the property value is valid (as it comes from the config).
     */
    @Override
    protected void updateWidgetValue() {
        this.widget.setValue(String.format("%.2f", configType.get()));
        this.widget.setTextColor(0xFFFFFFFF);
    }

    /**
     * Checks if the current value is equivalent to the default value.
     * <p>
     * Uses a small epsilon (0.0001) to handle floating-point precision errors during comparison.
     *
     * @return True if the current value is approximately equal to the default value.
     */
    @Override
    public boolean isDefault() {
        return Math.abs(configType.get() - configType.defaultValue) < 0.0001;
    }
}