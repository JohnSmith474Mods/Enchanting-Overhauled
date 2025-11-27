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

/**
 * The main configuration GUI screen.
 * <p>
 * This screen utilizes a {@link TabNavigationBar} to organize configuration settings into
 * distinct categories (tabs). It manages the overall layout, including the header,
 * footer buttons (Reset, Done), and the lifecycle of the configuration data (saving on close).
 */
public class ConfigScreen extends Screen {

    /**
     * The parent screen to return to when this configuration screen is closed.
     */
    private final Screen parent;

    /**
     * The configuration manager instance that provides the properties and handles I/O operations.
     */
    private final ConfigManager manager;

    /**
     * The manager responsible for handling tab switching and widget visibility.
     * It interacts directly with the screen's widget list to add/remove elements based on the selected tab.
     */
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);

    /**
     * The navigation bar UI component that displays the tab buttons at the top of the screen.
     */
    private TabNavigationBar tabNavigationBar;

    /**
     * A standardized layout manager that positions the header and footer elements.
     */
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    /**
     * The "Reset to Defaults" button located in the footer.
     * Its active state is toggled based on whether the current tab has modified values.
     */
    private Button resetButton;

    /**
     * Constructs a new configuration screen.
     *
     * @param parent  The screen to return to when this configuration screen is closed.
     * @param manager The {@link ConfigManager} instance holding the configuration state and structure.
     */
    public ConfigScreen(Screen parent, ConfigManager manager) {
        super(createStyledTitle(manager.modId));
        this.parent = parent;
        this.manager = manager;
        this.layout.setHeaderHeight(24);
    }

    /**
     * Generates a stylized title component for the screen.
     * <p>
     * This method splits the translated title string into two parts (if possible)
     * and applies different formatting colors to create a distinct visual style.
     *
     * @param modId The mod ID used to resolve the translation key {@code config.<modId>.title}.
     * @return A formatted {@link Component} representing the screen title.
     */
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

    /**
     * Initializes the screen's components, including the tab navigation bar and footer buttons.
     * <p>
     * This method iterates through the registered configuration tabs from the manager,
     * builds the UI structure, and sets the initial layout state.
     */
    @Override
    protected void init() {
        TabNavigationBar.Builder tabBuilder = TabNavigationBar.builder(this.tabManager, this.width);

        for (PropertyTab categoryTab : this.manager.getTabs()) {
            tabBuilder.addTabs(new johnsmith.enchantingoverhauled.api.config.client.gui.ConfigTab(this, categoryTab, this.manager));
        }

        this.tabNavigationBar = tabBuilder.build();

        this.addRenderableWidget(this.tabNavigationBar);

        String resetKey = "config." + this.manager.modId + ".reset";

        net.minecraft.client.gui.layouts.LinearLayout footerRow = net.minecraft.client.gui.layouts.LinearLayout.horizontal().spacing(8);
        this.resetButton = footerRow.addChild(Button.builder(Component.translatable(resetKey), button -> this.resetCurrentTab()).width(150).build());
        footerRow.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(150).build());
        this.layout.addToFooter(footerRow);

        this.layout.visitWidgets(this::addRenderableWidget);

        this.tabNavigationBar.selectTab(0, false);

        this.repositionElements();
        this.updateMasterResetButton();
    }

    /**
     * Recalculates the position and size of the screen elements.
     * <p>
     * This ensures the tab navigation bar spans the full width and the content area
     * is strictly bounded within the central column of the screen, preventing
     * widget overlap with the header or footer.
     */
    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar != null) {
            this.tabNavigationBar.setWidth(this.width);
            this.tabNavigationBar.arrangeElements();
        }

        this.layout.arrangeElements();

        int contentWidth = Math.min(400, this.width);

        int contentLeft = (this.width - contentWidth) / 2;

        int tabTop = this.layout.getHeaderHeight();
        int tabHeight = this.layout.getContentHeight();

        ScreenRectangle tabArea = new ScreenRectangle(contentLeft, tabTop, contentWidth, tabHeight);

        this.tabManager.setTabArea(tabArea);
    }

    /**
     * Resets all configuration entries in the currently selected tab to their default values.
     */
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

    /**
     * Updates the active state of the master "Reset" button in the footer.
     * <p>
     * The button is enabled only if at least one option in the current tab differs
     * from its default value.
     */
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

    /**
     * Closes the screen, triggers an asynchronous save of the configuration,
     * and returns to the parent screen.
     */
    @Override
    public void onClose() {
        this.manager.save().exceptionally(e -> {
            manager.logger.error("Failed to auto-save config on screen close", e);
            return null;
        });

        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}