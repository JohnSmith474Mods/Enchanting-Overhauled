package johnsmith.enchantingoverhauled.api.config.client.gui;

import johnsmith.enchantingoverhauled.api.config.ConfigManager;
import johnsmith.enchantingoverhauled.api.config.data.PropertyTab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Represents a single tab within the {@link ConfigScreen} corresponding to a {@link PropertyTab} category.
 * <p>
 * This class implements the {@link Tab} interface to define the tab's title and manage the layout
 * and interaction of its main content widget, which is the {@link ConfigList}.
 */
public class ConfigTab implements Tab {
    /**
     * The localized title displayed on the tab button.
     */
    private final Component title;

    /**
     * The list container holding all the configurable options for this tab.
     */
    final ConfigList list;

    /**
     * Constructs a new configuration tab, setting its localized title and initializing the content list.
     *
     * @param screen The parent configuration screen, used to set up the list.
     * @param category The data model for the tab, providing the translation key.
     * @param manager The central configuration manager, providing the data for the list.
     */
    public ConfigTab(ConfigScreen screen, PropertyTab category, ConfigManager manager) {
        this.title = Component.translatable(category.translationKey());
        this.list = new ConfigList(screen, Minecraft.getInstance(), category, manager);
    }

    /**
     * {@inheritDoc}
     *
     * @return The localized title of this tab.
     */
    @Override
    public @NotNull Component getTabTitle() {
        return this.title;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Forwards the main content widget (the {@link ConfigList}) to the consumer so it can be added to the screen.
     *
     * @param consumer The consumer that accepts widgets to be added to the screen.
     */
    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        consumer.accept(this.list);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Positions and resizes the internal {@link ConfigList} based on the provided screen rectangle.
     *
     * @param rectangle The screen area allocated for this tab's content.
     */
    @Override
    public void doLayout(ScreenRectangle rectangle) {
        this.list.updateSizeAndPosition(rectangle.width(), rectangle.height(), rectangle.top());
        this.list.setX(rectangle.left());
    }
}