package johnsmith.enchantingoverhauled.api.config.client.gui.entry;

import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.client.gui.ConfigList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * A configuration entry implementation for integer properties.
 * <p>
 * This entry renders an {@link EditBox} that accepts numeric input. It enforces
 * bounds validation and provides visual feedback (red text) for invalid input,
 * ensuring the user cannot set the value outside the defined integer range.
 */
public class IntEntry extends OptionEntry<Integer, EditBox> {

    /**
     * Constructs a new integer configuration entry.
     *
     * @param type           The bounded integer property backing this entry.
     * @param minecraft      The Minecraft client instance.
     * @param parentList     The parent configuration list containing this entry.
     * @param onValueChanged A callback runnable to execute when the value is modified by the user.
     */
    public IntEntry(Property.Bounded<Integer> type, Minecraft minecraft, ConfigList parentList, Runnable onValueChanged) {
        super(type, minecraft, parentList, onValueChanged);
        this.widget.setValue(String.valueOf(type.get()));
    }

    /**
     * Creates and configures the edit box widget for this entry.
     * <p>
     * The widget is configured with a filter to accept only digits and an optional
     * leading negative sign (`-`). The responder logic handles parsing the integer,
     * checking if it lies within the property's bounds, and updates the text
     * color based on validation success (white) or failure (red).
     *
     * @return A new {@link EditBox} instance.
     */
    @Override
    protected EditBox createWidget() {
        EditBox box = new EditBox(minecraft.font, 0, 0, 75, 20, Component.empty());
        box.setFilter(s -> s.matches("-?\\d*"));
        box.setResponder(s -> {
            try {
                if (s.isEmpty() || s.equals("-")) return;
                int val = Integer.parseInt(s);
                Property.Bounded<Integer> bounds = (Property.Bounded<Integer>) configType;

                if (val >= bounds.lowerBound && val <= bounds.upperBound) {
                    configType.set(val);
                    box.setTextColor(0xFFFFFF);
                } else {
                    box.setTextColor(0xFF0000);
                }
                updateResetButton();
                this.onValueChanged.run();
            } catch (NumberFormatException ignored) {
                box.setTextColor(0xFF0000);
            }
        });
        return box;
    }

    /**
     * Synchronizes the widget's text with the underlying property value.
     * <p>
     * Called when the property is reset or modified externally. This method
     * also resets the text color to white.
     */
    @Override
    protected void updateWidgetValue() {
        this.widget.setValue(String.valueOf(configType.get()));
        this.widget.setTextColor(0xFFFFFF);
    }
}