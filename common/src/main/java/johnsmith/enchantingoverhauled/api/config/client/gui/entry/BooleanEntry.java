package johnsmith.enchantingoverhauled.api.config.client.gui.entry;

import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.client.gui.ConfigList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;

/**
 * A configuration entry implementation for boolean properties.
 * <p>
 * This entry renders a {@link CycleButton} that toggles between "On" and "Off" states.
 * It handles updating the underlying {@link Property.Binary} value and invoking the
 * global change listener when the state changes.
 */
public class BooleanEntry extends OptionEntry<Boolean, CycleButton<Boolean>> {

    /**
     * Constructs a new boolean configuration entry.
     *
     * @param type           The boolean property backing this entry.
     * @param minecraft      The Minecraft client instance.
     * @param parentList     The parent configuration list containing this entry.
     * @param onValueChanged A callback runnable to execute when the value is modified by the user.
     */
    public BooleanEntry(Property.Binary type, Minecraft minecraft, ConfigList parentList, Runnable onValueChanged) {
        super(type, minecraft, parentList, onValueChanged);
    }

    /**
     * Creates and configures the cycle button widget for this entry.
     * <p>
     * The button is initialized with the current value of the property.
     * Its listener updates the property value, refreshes the reset button state,
     * and triggers the screen's change callback.
     *
     * @return A new {@link CycleButton} instance.
     */
    @Override
    protected CycleButton<Boolean> createWidget() {
        return CycleButton.onOffBuilder(configType.get())
                .displayOnlyValue()
                .create(0, 0, 75, 20, Component.empty(), (b, val) -> {
                    configType.set(val);
                    updateResetButton();
                    this.onValueChanged.run();
                });
    }

    /**
     * Synchronizes the widget's display value with the underlying property value.
     * <p>
     * Called when the property is reset or modified externally.
     */
    @Override
    protected void updateWidgetValue() {
        this.widget.setValue(configType.get());
    }
}