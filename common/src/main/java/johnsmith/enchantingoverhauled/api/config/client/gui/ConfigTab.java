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

public class ConfigTab implements Tab {
    private final Component title;
    final ConfigList list;

    public ConfigTab(ConfigScreen screen, PropertyTab category, ConfigManager manager) {
        this.title = Component.translatable(category.translationKey());
        this.list = new ConfigList(screen, Minecraft.getInstance(), category, manager);
    }

    @Override
    public @NotNull Component getTabTitle() {
        return this.title;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        consumer.accept(this.list);
    }

    @Override
    public void doLayout(ScreenRectangle rectangle) {
        this.list.updateSizeAndPosition(rectangle.width(), rectangle.height(), rectangle.top());
        this.list.setX(rectangle.left());
    }
}