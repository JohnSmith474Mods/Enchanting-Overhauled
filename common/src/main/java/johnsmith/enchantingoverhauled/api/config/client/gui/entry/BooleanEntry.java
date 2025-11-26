package johnsmith.enchantingoverhauled.api.config.client.gui.entry;

import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.client.gui.ConfigList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;

public class BooleanEntry extends OptionEntry<Boolean, CycleButton<Boolean>> {

    public BooleanEntry(Property.Binary type, Minecraft minecraft, ConfigList parentList, Runnable onValueChanged) {
        super(type, minecraft, parentList, onValueChanged);
    }

    @Override
    protected CycleButton<Boolean> createWidget() {
        return CycleButton.onOffBuilder(configType.get())
                .displayOnlyValue()
                .create(0, 0, 75, 20, Component.empty(), (b, val) -> {
                    configType.set(val);
                    updateResetButton();
                    // Use the callback passed from the screen
                    this.onValueChanged.run();
                });
    }

    @Override
    protected void updateWidgetValue() {
        this.widget.setValue(configType.get());
    }
}