package johnsmith.enchantingoverhauled.api.config.client.gui.entry;

import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.client.gui.ConfigList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class IntEntry extends OptionEntry<Integer, EditBox> {

    public IntEntry(Property.Bounded<Integer> type, Minecraft minecraft, ConfigList parentList, Runnable onValueChanged) {
        super(type, minecraft, parentList, onValueChanged);
        this.widget.setValue(String.valueOf(type.get()));
    }

    @Override
    protected EditBox createWidget() {
        // 'minecraft' is now accessible from the superclass
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

    @Override
    protected void updateWidgetValue() {
        this.widget.setValue(String.valueOf(configType.get()));
        this.widget.setTextColor(0xFFFFFF);
    }
}