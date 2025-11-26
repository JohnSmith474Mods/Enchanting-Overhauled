package johnsmith.enchantingoverhauled.api.config.client.gui;

import johnsmith.enchantingoverhauled.api.config.data.PropertyGroup;
import johnsmith.enchantingoverhauled.api.config.data.PropertyTab;
import johnsmith.enchantingoverhauled.api.config.ConfigManager;
import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.client.gui.entry.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public class ConfigList extends ContainerObjectSelectionList<Entry> {
    private final ConfigScreen screen;

    public ConfigList(ConfigScreen screen, Minecraft minecraft, PropertyTab specificTab, ConfigManager manager) {
        super(minecraft, screen.width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 20);
        this.screen = screen;

        // Iterate Groups from the Manager Instance
        for (PropertyGroup group : manager.getGroups(specificTab)) {
            addEntry(new CategoryEntry(group, minecraft));

            // Iterate Configs from the Manager Instance
            for (Property<?> config : manager.getConfigs(group)) {
                addConfigEntry(config);
            }
        }
    }

    // --- Fixes for Visual Artifacts ---
    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
        // Do nothing -> Transparent background
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
        // Do nothing -> No shadows/outlines
    }

    // --- Public API for Entries ---
    /**
     * Exposes the scrollbar position to external Entry classes
     * so they can align their widgets correctly.
     */
    public int getScrollbarX() {
        return this.getScrollbarPosition();
    }

    @Override
    public void updateSizeAndPosition(int width, int height, int top) {
        super.updateSizeAndPosition(width, height, top);
    }

    @SuppressWarnings("unchecked")
    private void addConfigEntry(Property<?> config) {
        // Pass dependencies: (ConfigType, Minecraft, ParentList, Callback)
        if (config instanceof Property.Binary binary) {
            addEntry(new BooleanEntry(binary, minecraft, this, screen::updateMasterResetButton));
        }
        else if (config instanceof Property.Bounded<?> bounded) {
            Object def = bounded.defaultValue;
            if (def instanceof Integer) {
                addEntry(new IntEntry((Property.Bounded<Integer>) bounded, minecraft, this, screen::updateMasterResetButton));
            } else if (def instanceof Double) {
                addEntry(new DoubleEntry((Property.Bounded<Double>) bounded, minecraft, this, screen::updateMasterResetButton));
            } else if (def instanceof Float) {
                addEntry(new FloatEntry((Property.Bounded<Float>) bounded, minecraft, this, screen::updateMasterResetButton));
            }
        }
    }

    @Override
    public int getRowWidth() { return 340; }

    @Override
    protected int getScrollbarPosition() { return super.getScrollbarPosition() - 3; }
}