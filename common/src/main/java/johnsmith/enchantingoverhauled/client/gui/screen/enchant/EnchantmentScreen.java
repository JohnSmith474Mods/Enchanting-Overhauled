package johnsmith.enchantingoverhauled.client.gui.screen.enchant;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.client.gui.widget.enchant.EnchantWidget;
import johnsmith.enchantingoverhauled.client.gui.widget.enchant.EnchantWidgetFactory;
import johnsmith.enchantingoverhauled.client.gui.widget.enchant.PageNavigationWidget;
import johnsmith.enchantingoverhauled.client.gui.widget.enchant.RerollWidget;
import johnsmith.enchantingoverhauled.client.gui.tooltip.enchant.EnchantTooltip;
import johnsmith.enchantingoverhauled.client.gui.tooltip.enchant.EnchantTooltipFactory;
import johnsmith.enchantingoverhauled.client.gui.tooltip.enchant.EnchantTooltipContext;
import johnsmith.enchantingoverhauled.menu.enchant.EnchantmentMenu;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantContext;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantRenderState;
import johnsmith.enchantingoverhauled.menu.enchant.controller.EnchantRenderStateFactory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.NotNull;

/**
 * The main Graphical User Interface (GUI) screen for the custom Enchanting Altar.
 * <p>
 * This screen coordinates the rendering and interaction of various widgets including
 * the scrollable enchantment list, the reroll button, page navigation, and the experience bar.
 * It observes the underlying {@link EnchantmentMenu} for state changes.
 */
public class EnchantmentScreen extends AbstractContainerScreen<EnchantmentMenu> {

    // region Constants
    private static final ResourceLocation GALACTIC_FONT_ID = ResourceLocation.fromNamespaceAndPath("minecraft", "alt");
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/background.png");

    private static final ResourceLocation EXPERIENCE_BAR_ACTIVE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/experience_bar/full.png");
    private static final ResourceLocation EXPERIENCE_BAR_INACTIVE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/experience_bar/empty.png");

    private static final ResourceLocation EMPTY_SLOT_TARGET_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/slot/target.png");
    private static final ResourceLocation EMPTY_SLOT_FUEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/slot/fuel.png");
    private static final ResourceLocation EMPTY_SLOT_SOURCE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/slot/source.png");

    private static final int SCREEN_ORIGINAL_HEIGHT = 166;
    private static final int ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR = 0x80FF20;
    // endregion

    // region Fields
    private final MenuLayoutConfig layout = MenuLayoutConfig.DEFAULT;
    private final List<EnchantWidget> enchantmentWidgets = new ArrayList<>();

    private RerollWidget rerollWidget;
    private PageNavigationWidget pageNavigationWidget;

    private int currentScrollOffset = 0;
    private int lastContextHash = -1;
    private int lastSeed = -1;
    // endregion

    /**
     * Constructs a new Enchantment Screen.
     *
     * @param menu            The server-side menu container bridging the inventory.
     * @param playerInventory The player's current inventory.
     * @param title           The title component of the screen (typically modified to use the galactic font).
     */
    public EnchantmentScreen(EnchantmentMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, modifyTitle(title));
    }

    /**
     * Helper method to convert the screen's title into the yellow, standard galactic alphabet format.
     *
     * @param originalTitle The base title component.
     * @return A copy of the title formatted with the galactic font and yellow color.
     */
    private static Component modifyTitle(Component originalTitle) {
        if (originalTitle != null) {
            Style galacticStyle = originalTitle.getStyle().withFont(GALACTIC_FONT_ID);
            return originalTitle.copy().setStyle(galacticStyle).withStyle(ChatFormatting.YELLOW);
        }
        return Component.empty();
    }

    // region Initialization & Lifecycle

    /**
     * Initializes the UI components, calculates bounds, and configures the child widgets
     * (Reroll, Navigation) when the screen is first opened or resized.
     */
    @Override
    protected void init() {
        super.init();
        this.imageHeight = SCREEN_ORIGINAL_HEIGHT + this.layout.screenHeightAdjustment();

        this.rerollWidget = new RerollWidget(this.menu, this.layout, () -> {
            if (this.minecraft != null && this.minecraft.gameMode != null && this.minecraft.player != null) {
                if (this.menu.clickMenuButton(this.minecraft.player, 0)) {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                }
            }
        });

        this.pageNavigationWidget = new PageNavigationWidget(this.menu, this.layout,
                () -> this.currentScrollOffset,
                offset -> this.currentScrollOffset = offset,
                this::refreshWidgets
        );

        this.lastSeed = this.menu.getEnchantmentSeed();
        this.refreshWidgets();
    }

    /**
     * Ticks the screen, tracking changes to the enchantment seed or available contexts
     * to trigger a visual refresh. Discards pagination offset if the target item is removed.
     */
    @Override
    public void containerTick() {
        super.containerTick();

        int currentSeed = this.menu.getEnchantmentSeed();
        if (currentSeed != this.lastSeed) {
            this.lastSeed = currentSeed;
            this.menu.slotsChanged(this.menu.getSlot(0).container);
        }

        List<EnchantContext> contexts = this.menu.getActiveContexts();
        int currentHash = contexts.hashCode();

        if (currentHash != this.lastContextHash) {
            Constants.LOG.debug("EnchantmentScreen: Context hash changed from {} to {}. Refreshing widgets.", this.lastContextHash, currentHash);
            this.lastContextHash = currentHash;
            this.refreshWidgets();
        }

        if (!this.menu.getSlot(0).hasItem()) {
            this.currentScrollOffset = 0;
            if (!this.enchantmentWidgets.isEmpty()) {
                this.refreshWidgets();
            }
        }
    }

    /**
     * Synchronizes the visible {@link EnchantWidget} objects with the underlying data model.
     * Evaluates which contexts are currently visible given the scroll offset, creating or mutating
     * the stateful widget components to match.
     */
    public void refreshWidgets() {
        List<EnchantContext> contexts = this.menu.getActiveContexts();
        int totalEnchants = contexts.size();

        Constants.LOG.debug("EnchantmentScreen: Refreshing Widgets. Active contexts count: {}", totalEnchants);

        int maxOffset = Math.max(0, totalEnchants - this.layout.pageSize());
        if (this.currentScrollOffset > maxOffset) {
            this.currentScrollOffset = maxOffset;
        }

        // Initialize widget list if needed
        if (this.enchantmentWidgets.size() != this.layout.pageSize()) {
            this.enchantmentWidgets.clear();
            for (int i = 0; i < this.layout.pageSize(); i++) {
                this.enchantmentWidgets.add(null);
            }
        }

        for (int i = 0; i < this.layout.pageSize(); i++) {
            int realIndex = this.currentScrollOffset + i;
            EnchantRenderState state;
            int slotSeed;

            if (realIndex >= totalEnchants) {
                state = EnchantRenderStateFactory.buildEmptyState();
                slotSeed = i;
            } else {
                EnchantContext context = contexts.get(realIndex);
                state = EnchantRenderStateFactory.createFrom(context);
                slotSeed = context.slotIndex();
            }

            int buttonX = this.leftPos + this.layout.enchantingButtonXOffset();
            int buttonY = this.topPos + this.layout.enchantingButtonYOffset() + (this.layout.enchantingButtonHeight() * i);

            RandomSource deterministicRandom = RandomSource.create((long) this.menu.getEnchantmentSeed() + slotSeed);
            int texIndex = deterministicRandom.nextInt(10);
            boolean usePlain = Config.USE_PLAIN_BACKGROUND.get();

            Runnable action = () -> {
                if (realIndex < totalEnchants && this.minecraft != null && this.minecraft.gameMode != null) {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, realIndex + 1);
                }
            };

            EnchantWidget currentWidget = this.enchantmentWidgets.get(i);

            // Stateful mutation: Only recreate the widget object if the visual state archetype changed
            if (currentWidget != null && currentWidget.getState().state() == state.state()) {
                currentWidget.setState(state);
                currentWidget.setTexIndex(texIndex);
                currentWidget.setOnPressAction(action);
                currentWidget.updatePosition(buttonX, buttonY);
            } else {
                this.enchantmentWidgets.set(i, EnchantWidgetFactory.createFrom(buttonX, buttonY, state, texIndex, usePlain, action));
            }
        }
    }
    // endregion

    // region Rendering

    /**
     * Renders the background texture, empty slot ghosts, widgets, and the experience bar.
     *
     * @param context     The GUI graphics context.
     * @param partialTick The partial tick time for animations.
     * @param mouseX      The current mouse X coordinate.
     * @param mouseY      The current mouse Y coordinate.
     */
    @Override
    protected void renderBg(GuiGraphics context, float partialTick, int mouseX, int mouseY) {
        int alignX = this.leftPos;
        int alignY = this.topPos;

        context.blit(BACKGROUND_TEXTURE, alignX, alignY, 0, 0, this.imageWidth, this.imageHeight);

        if (!this.menu.getSlot(0).hasItem()) {
            context.blit(EMPTY_SLOT_TARGET_TEXTURE, alignX + this.menu.getSlot(0).x, alignY + this.menu.getSlot(0).y, 0, 0, 16, 16, 16, 16);
        }
        if (!this.menu.getSlot(1).hasItem()) {
            context.blit(EMPTY_SLOT_FUEL_TEXTURE, alignX + this.menu.getSlot(1).x, alignY + this.menu.getSlot(1).y, 0, 0, 16, 16, 16, 16);
        }
        if (!this.menu.getSlot(2).hasItem()) {
            context.blit(EMPTY_SLOT_SOURCE_TEXTURE, alignX + this.menu.getSlot(2).x, alignY + this.menu.getSlot(2).y, 0, 0, 16, 16, 16, 16);
        }

        for (EnchantWidget widget : this.enchantmentWidgets) {
            if (widget != null) widget.render(context, mouseX, mouseY, partialTick);
        }

        int lapisCount = this.menu.getSlot(1).getItem().getCount();

        this.rerollWidget.updatePosition(alignX, alignY);
        this.rerollWidget.render(context, mouseX, mouseY, partialTick, lapisCount, this.minecraft);

        this.pageNavigationWidget.updatePosition(alignX, alignY);
        this.pageNavigationWidget.render(context, mouseX, mouseY, partialTick);

        this.drawExperienceBar(context, alignX, alignY);
    }

    /**
     * Renders the labels for the screen, adjusting inventory title placement due to height adjustments.
     *
     * @param graphics The GUI graphics context.
     * @param mouseX   The current mouse X coordinate.
     * @param mouseY   The current mouse Y coordinate.
     */
    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY + this.layout.screenHeightAdjustment(), 4210752, false);
    }

    /**
     * Top-level render call. Ensures the background is drawn, delegates to super for children,
     * and evaluates tooltips based on the hovered widget.
     *
     * @param context     The GUI graphics context.
     * @param mouseX      The current mouse X coordinate.
     * @param mouseY      The current mouse Y coordinate.
     * @param partialTick The partial tick time for animations.
     */
    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        super.render(context, mouseX, mouseY, partialTick);
        this.renderTooltip(context, mouseX, mouseY);

        boolean isCreative = this.minecraft != null && this.minecraft.player != null && this.minecraft.player.isCreative();
        int lapisCount = this.menu.getSlot(1).getItem().getCount();

        boolean tooltipDrawn = this.drawEnchantmentSlotTooltips(context, mouseX, mouseY, lapisCount, isCreative);

        if (!tooltipDrawn) {
            this.rerollWidget.renderTooltip(context, mouseX, mouseY, this.font, lapisCount, this.minecraft);
        }
    }
    // endregion

    // region Input Handling

    /**
     * Delegates mouse click events to child components (reroll widget, enchantment slots, pagination).
     *
     * @param mouseX The X coordinate of the mouse.
     * @param mouseY The Y coordinate of the mouse.
     * @param button The mouse button being clicked.
     * @return true if the event was consumed, false otherwise.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.rerollWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        for (EnchantWidget widget : this.enchantmentWidgets) {
            if (widget != null && widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        if (this.pageNavigationWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Handles mouse wheel scrolling, delegating to the pagination widget to scroll the view.
     *
     * @param mouseX           The X coordinate of the mouse.
     * @param mouseY           The Y coordinate of the mouse.
     * @param horizontalAmount The horizontal scroll delta.
     * @param verticalAmount   The vertical scroll delta.
     * @return true if the scroll event was consumed, false otherwise.
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.pageNavigationWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    // endregion

    // region Helper Methods

    /**
     * Draws the player's experience bar natively within the UI, adjusting visual fullness
     * based on their current experience progress.
     *
     * @param context The graphics context.
     * @param alignX  The left screen alignment offset.
     * @param alignY  The top screen alignment offset.
     */
    private void drawExperienceBar(GuiGraphics context, int alignX, int alignY) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        int barX = alignX + this.layout.experienceBarXOffset();
        int barY = alignY + this.layout.experienceBarYOffset();

        context.blit(EXPERIENCE_BAR_INACTIVE, barX, barY, 0, 0, this.layout.experienceBarWidth(), this.layout.experienceBarHeight(), this.layout.experienceBarWidth(), this.layout.experienceBarHeight());

        float progress = this.minecraft.player.experienceProgress;
        int activeWidth = (int) (progress * (float) this.layout.experienceBarWidth());

        if (activeWidth > 0) {
            context.blit(EXPERIENCE_BAR_ACTIVE, barX, barY, 0, 0, activeWidth, this.layout.experienceBarHeight(), this.layout.experienceBarWidth(), this.layout.experienceBarHeight());
        }

        int playerLevel = this.minecraft.player.experienceLevel;
        if (playerLevel > 0) {
            String levelString = "" + playerLevel;
            int textX = barX + (this.layout.experienceBarWidth() / 2) - (this.font.width(levelString) / 2);
            int textY = alignY + this.layout.experienceBarYOffset() - 6;
            int outlineColor = 0;

            context.drawString(this.font, levelString, textX + 1, textY, outlineColor, false);
            context.drawString(this.font, levelString, textX - 1, textY, outlineColor, false);
            context.drawString(this.font, levelString, textX, textY + 1, outlineColor, false);
            context.drawString(this.font, levelString, textX, textY - 1, outlineColor, false);
            context.drawString(this.font, levelString, textX, textY, ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR, false);
        }
    }

    /**
     * Determines which widget is hovered and delegates rendering of its specific tooltip data.
     * Assembles the {@link EnchantTooltipContext} and generates the correct command instance.
     *
     * @param context    The GUI graphics context.
     * @param mouseX     The mouse X coordinate.
     * @param mouseY     The mouse Y coordinate.
     * @param lapisCount The amount of fuel the player has.
     * @param isCreative True if the player is in creative mode (bypasses costs).
     * @return true if a tooltip was successfully drawn, false otherwise.
     */
    private boolean drawEnchantmentSlotTooltips(GuiGraphics context, int mouseX, int mouseY, int lapisCount, boolean isCreative) {
        List<EnchantContext> contexts = this.menu.getActiveContexts();

        for (int i = 0; i < this.enchantmentWidgets.size(); i++) {
            EnchantWidget widget = this.enchantmentWidgets.get(i);
            if (widget != null && widget.isHovered) {
                EnchantRenderState state = widget.getState();
                if (state.enchantment().isEmpty()) {
                    return false;
                }

                int realIndex = this.currentScrollOffset + i;
                int pendingLevel = state.displayLevel();
                if (realIndex < contexts.size()) {
                    pendingLevel = contexts.get(realIndex).pending().enchantment().map(e -> e.level).orElse(state.displayLevel());
                }

                int levelContext = switch (state.state()) {
                    case MAXED, OVERLEVELED, LOCKED -> state.displayLevel();
                    default -> switch (state.visualSource()) {
                        case TARGET -> state.displayLevel();
                        case SOURCE, TABLE -> pendingLevel;
                        case NONE -> state.displayLevel();
                    };
                };

                EnchantTooltipContext tooltipCtx = new EnchantTooltipContext(
                        this.font,
                        lapisCount,
                        isCreative,
                        state.enchantment().get(),
                        levelContext,
                        state.xpRequirement(),
                        state.xpCost(),
                        this.minecraft
                );

                EnchantTooltip command = EnchantTooltipFactory.create(state, tooltipCtx);
                return command.render(context, mouseX, mouseY);
            }
        }
        return false;
    }
    // endregion
}