package johnsmith.enchantingoverhauled.api.config.client.gui;

import johnsmith.enchantingoverhauled.api.config.client.gui.entry.OptionEntry;
import johnsmith.enchantingoverhauled.api.config.data.PropertyTab;
import johnsmith.enchantingoverhauled.api.config.ConfigManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final ConfigManager manager;

    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private TabNavigationBar tabNavigationBar;
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private Button resetButton;

    public ConfigScreen(Screen parent, ConfigManager manager) {
        super(createStyledTitle(manager.modId));
        this.parent = parent;
        this.manager = manager;
        this.layout.setHeaderHeight(24);
    }

    private static Component createStyledTitle(String modId) {
        String key = "config." + modId + ".title";
        String fullText = Component.translatable(key).getString();
        String[] parts = fullText.split("\\s+", 2);

        if (parts.length >= 2) {
            return Component.literal(parts[0])
                    .withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD)
                    .append(Component.literal(" "))
                    .append(Component.literal(parts[1])
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        }
        return Component.literal(fullText).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD);
    }

    @Override
    protected void init() {
        // We do NOT add a title header because the tabs occupy the top of the screen.

        // 1. Build the Tabs
        TabNavigationBar.Builder tabBuilder = TabNavigationBar.builder(this.tabManager, this.width);

        // Iterate Tabs from the Manager Instance instead of static Registry
        for (PropertyTab categoryTab : this.manager.getTabs()) {
            // Pass the manager to the Tab so it can build the ConfigList
            tabBuilder.addTabs(new johnsmith.enchantingoverhauled.api.config.client.gui.ConfigTab(this, categoryTab, this.manager));
        }

        this.tabNavigationBar = tabBuilder.build();

        // Add directly as widget (TabNavigationBar sits at Y=0)
        this.addRenderableWidget(this.tabNavigationBar);

        // 2. Footer Buttons
        // Dynamic translation key based on mod ID
        String resetKey = "config." + this.manager.modId + ".reset";

        net.minecraft.client.gui.layouts.LinearLayout footerRow = net.minecraft.client.gui.layouts.LinearLayout.horizontal().spacing(8);
        this.resetButton = footerRow.addChild(Button.builder(Component.translatable(resetKey), button -> this.resetCurrentTab()).width(150).build());
        footerRow.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(150).build());
        this.layout.addToFooter(footerRow);

        // 3. Finalize Layout
        this.layout.visitWidgets(this::addRenderableWidget);

        // Select the first tab by default
        this.tabNavigationBar.selectTab(0, false);

        this.repositionElements();
        this.updateMasterResetButton();
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar != null) {
            this.tabNavigationBar.setWidth(this.width);
            this.tabNavigationBar.arrangeElements();
        }

        this.layout.arrangeElements();

        // 1. Determine the content width (Match Tab Bar ~400px)
        int contentWidth = Math.min(400, this.width);

        // 2. Calculate centered X position
        int contentLeft = (this.width - contentWidth) / 2;

        // 3. Define Vertical bounds (Header to Footer)
        int tabTop = this.layout.getHeaderHeight();
        int tabHeight = this.layout.getContentHeight();

        // 4. Create the constrained rectangle
        ScreenRectangle tabArea = new ScreenRectangle(contentLeft, tabTop, contentWidth, tabHeight);

        this.tabManager.setTabArea(tabArea);
    }

    private void resetCurrentTab() {
        if (this.tabManager.getCurrentTab() instanceof johnsmith.enchantingoverhauled.api.config.client.gui.ConfigTab configTab) {
            for (ContainerObjectSelectionList.Entry<?> entry : configTab.list.children()) {
                if (entry instanceof OptionEntry<?, ?> option) {
                    option.reset();
                }
            }
        }
        this.updateMasterResetButton();
    }

    public void updateMasterResetButton() {
        boolean canReset = false;
        if (this.tabManager.getCurrentTab() instanceof johnsmith.enchantingoverhauled.api.config.client.gui.ConfigTab configTab) {
            for (ContainerObjectSelectionList.Entry<?> entry : configTab.list.children()) {
                if (entry instanceof OptionEntry<?, ?> option && !option.isDefault()) {
                    canReset = true;
                    break;
                }
            }
        }
        if (this.resetButton != null) {
            this.resetButton.active = canReset;
        }
    }

    @Override
    public void onClose() {
        // NON-BLOCKING SAVE:
        // We trigger the save process on a background thread.
        // If the user immediately closes the game, the JVM shutdown hooks (if any)
        // or the OS usually allow small file writes to finish, but strictly speaking,
        // this is "fire and forget".

        this.manager.save().exceptionally(e -> {
            manager.logger.error("Failed to auto-save config on screen close", e);
            return null;
        });

        // Return to parent screen immediately without waiting for disk I/O
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}