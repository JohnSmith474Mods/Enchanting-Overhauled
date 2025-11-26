package johnsmith.enchantingoverhauled.api.config.client.gui.entry;

import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.client.gui.ConfigList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class DoubleEntry extends OptionEntry<Double, EditBox> {

    public DoubleEntry(Property.Bounded<Double> type, Minecraft minecraft, ConfigList parentList, Runnable onValueChanged) {
        super(type, minecraft, parentList, onValueChanged);
        this.widget.setValue(String.format("%.2f", type.get()));
    }

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

    @Override
    protected void updateWidgetValue() {
        this.widget.setValue(String.format("%.2f", configType.get()));
        this.widget.setTextColor(0xFFFFFF);
    }

    @Override
    public boolean isDefault() {
        return Math.abs(configType.get() - configType.defaultValue) < 0.0001;
    }
}