package johnsmith.enchantingoverhauled.api.config.client.gui;

import johnsmith.enchantingoverhauled.api.config.data.PropertyGroup;
import johnsmith.enchantingoverhauled.api.config.data.PropertyTab;
import johnsmith.enchantingoverhauled.api.config.ConfigManager;
import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.client.gui.entry.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

/**
 * A custom implementation of {@link ContainerObjectSelectionList} used to render
 * the list of configuration categories and adjustable properties for a single {@link ConfigTab}.
 * <p>
 * This list dynamically populates itself based on the structure defined in the {@link ConfigManager},
 * grouping individual properties under category headers. It overrides rendering methods to
 * provide a transparent background suitable for overlaying other GUI elements.
 */
public class ConfigList extends ContainerObjectSelectionList<Entry> {
    private final ConfigScreen screen;

    /**
     * Constructs a new configuration list.
     *
     * @param screen      The parent configuration screen, used for accessing layout dimensions and callbacks.
     * @param minecraft   The Minecraft client instance.
     * @param specificTab The tab containing the groups and properties to display.
     * @param manager     The central configuration manager instance.
     */
    public ConfigList(ConfigScreen screen, Minecraft minecraft, PropertyTab specificTab, ConfigManager manager) {
        super(minecraft, screen.width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 20);
        this.screen = screen;

        for (PropertyGroup group : manager.getGroups(specificTab)) {
            addEntry(new CategoryEntry(group, minecraft));

            for (Property<?> config : manager.getConfigs(group)) {
                addConfigEntry(config);
            }
        }
    }

    /**
     * Overridden to prevent rendering a background texture, making the list transparent.
     *
     * @param guiGraphics The graphics context for rendering.
     */
    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }

    /**
     * Overridden to prevent rendering list separators (shadows/outlines).
     *
     * @param guiGraphics The graphics context for rendering.
     */
    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
    }

    /**
     * Exposes the calculated position of the scrollbar for external {@link OptionEntry} classes
     * to correctly align their widgets relative to the right edge of the content area.
     *
     * @return The x-coordinate of the scrollbar position.
     */
    public int getScrollbarX() {
        return this.scrollBarX();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSizeAndPosition(int width, int height, int top) {
        super.updateSizeAndPosition(width, height, top);
    }

    /**
     * Creates and adds the appropriate {@link OptionEntry} subclass for a given {@link Property}.
     *
     * @param config The property object to create an entry for.
     */
    @SuppressWarnings("unchecked")
    private void addConfigEntry(Property<?> config) {
        if (config instanceof Property.Binary binary) {
            addEntry(new BooleanEntry(binary, minecraft, this, screen::updateMasterResetButton));
        } else if (config instanceof Property.Bounded<?> bounded) {
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

    /**
     * Specifies a fixed width for the content rows within the list.
     *
     * @return The width of the content area (340 pixels).
     */
    @Override
    public int getRowWidth() { return 340; }

    /**
     * Adjusts the calculated scrollbar position to align it visually.
     *
     * @return The adjusted scrollbar x-coordinate.
     */
    @Override
    protected int scrollBarX() { return super.scrollBarX() - 3; }
}