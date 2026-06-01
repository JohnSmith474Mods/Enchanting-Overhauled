package johnsmith.enchantingoverhauled.client.gui.screen;

import johnsmith.enchantingoverhauled.menu.EnchantmentMenu;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

// Custom Imports
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.platform.Services;
import org.jetbrains.annotations.NotNull;

public class EnchantmentScreen extends AbstractContainerScreen<EnchantmentMenu> {

    // region Vanilla Constants (Kept for compatibility/references)
    private static final ResourceLocation[] ENABLED_LEVEL_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3")};
    private static final ResourceLocation[] DISABLED_LEVEL_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1_disabled"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2_disabled"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3_disabled")};
    // endregion

    // region Overhaul Constants
    private static final ResourceLocation GALACTIC_FONT_ID = ResourceLocation.fromNamespaceAndPath("minecraft", "alt");

    // Textures
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/background.png");
    private static final ResourceLocation REROLL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/reroll.png");
    private static final ResourceLocation REROLL_HIGHLIGHTED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/reroll.png");
    private static final ResourceLocation REROLL_DISABLED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/reroll.png");
    private static final ResourceLocation ENCHANTMENT_SLOT_MAXED_OUT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/maxed_out.png");
    private static final ResourceLocation ENCHANTMENT_SLOT_OVER_ENCHANTED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/over_enchanted.png");
    private static final ResourceLocation EXPERIENCE_BAR_ACTIVE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/experience_bar/full.png");
    private static final ResourceLocation EXPERIENCE_BAR_INACTIVE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/experience_bar/empty.png");
    private static final ResourceLocation PAGE_FORWARD_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/forward.png");
    private static final ResourceLocation PAGE_FORWARD_HIGHLIGHTED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/forward.png");
    private static final ResourceLocation PAGE_FORWARD_DISABLED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/forward.png");
    private static final ResourceLocation PAGE_BACKWARD_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/backward.png");
    private static final ResourceLocation PAGE_BACKWARD_HIGHLIGHTED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/backward.png");
    private static final ResourceLocation PAGE_BACKWARD_DISABLED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/backward.png");
    private static final ResourceLocation EMPTY_SLOT_TARGET_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/slot/target.png");
    private static final ResourceLocation EMPTY_SLOT_FUEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/slot/fuel.png");
    private static final ResourceLocation EMPTY_SLOT_SOURCE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/slot/source.png");

    // Layout
    private static final int SCREEN_ORIGINAL_HEIGHT = 166;
    private static final int SCREEN_HEIGHT_ADJUSTMENT = 29;

    // Buttons
    private static final int REROLL_BUTTON_ID = 0;
    private static final int REROLL_BUTTON_X_OFFSET = 15 - 7;
    private static final int REROLL_BUTTON_Y_OFFSET = 17;
    private static final int REROLL_BUTTON_WIDTH = 36;
    private static final int REROLL_BUTTON_HEIGHT = 18;
    private static final int REROLL_COST_WIDTH = 16;
    private static final int REROLL_COST_HEIGHT = 16;
    private static final int REROLL_COST_X_OFFSET = -1;
    private static final int REROLL_COST_Y_OFFSET = 1;

    private static final int ENCHANTING_BUTTON_WIDTH = 108 + 7;
    private static final int ENCHANTING_BUTTON_HEIGHT = 18;
    private static final int ENCHANTING_BUTTON_X_OFFSET = 60 - 7 - 1;
    private static final int ENCHANTING_BUTTON_Y_OFFSET = 17 + 2;
    private static final int ENCHANTING_COST_HEIGHT = 16;
    private static final int ENCHANTING_COST_WIDTH = 16;
    private static final int ENCHANTING_COST_X_OFFSET = 0;
    private static final int ENCHANTING_COST_Y_OFFSET = 2;
    private static final int ENCHANTING_POWER_X_OFFSET = 86 + 11;
    private static final int ENCHANTING_POWER_Y_OFFSET = 8;
    private static final int ENCHANTING_TEXT_X_OFFSET = ENCHANTING_COST_WIDTH;
    private static final int ENCHANTING_TEXT_Y_OFFSET = 5;
    private static final int ENCHANTING_TEXT_MAX_WIDTH = ENCHANTING_BUTTON_WIDTH - ENCHANTING_TEXT_X_OFFSET - 2;

    private static final int PAGE_SIZE = 3;
    private static final int BACKWARD_BUTTON_WIDTH = 9;
    private static final int BACKWARD_BUTTON_HEIGHT = 10;
    private static final int FORWARD_BUTTON_WIDTH = 10;
    private static final int FORWARD_BUTTON_HEIGHT = 10;
    private static final int BACKWARD_X_OFFSET = 100;
    private static final int BACKWARD_Y_OFFSET = 5;
    private static final int FORWARD_X_OFFSET = BACKWARD_X_OFFSET + BACKWARD_BUTTON_WIDTH;
    private static final int FORWARD_Y_OFFSET = BACKWARD_Y_OFFSET;

    // Experience Bar
    private static final int EXPERIENCE_BAR_X_OFFSET = 60 - 1;
    private static final int EXPERIENCE_BAR_Y_OFFSET = 80 + 5;
    private static final int EXPERIENCE_BAR_WIDTH = 102;
    private static final int EXPERIENCE_BAR_HEIGHT = 5;

    // Text Colors
    private static final int ENCHANTMENT_MAXED_OUT_TEXT_COLOR = 0xF2F09D;
    private static final int ENCHANTMENT_OVER_ENCHANTED_TEXT_COLOR = 0x4D2299;
    private static final int ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR = 0x80FF20;
    private static final int ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR = 0x408000;
    // endregion

    public int time;
    private int currentScrollOffset = 0;

    public EnchantmentScreen(EnchantmentMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, modifyTitle(title));
    }

    /**
     * Applies the galactic style and color to the title before passing it to the super constructor.
     */
    private static Component modifyTitle(Component originalTitle) {
        if (originalTitle != null) {
            Style galacticStyle = originalTitle.getStyle().withFont(GALACTIC_FONT_ID);
            return originalTitle.copy().setStyle(galacticStyle).withStyle(ChatFormatting.YELLOW);
        } return Component.empty();
    }

    protected void init() {
        super.init();
        this.imageHeight = SCREEN_ORIGINAL_HEIGHT + SCREEN_HEIGHT_ADJUSTMENT;
    }

    public void containerTick() {
        super.containerTick();
        if (!this.menu.getSlot(0).hasItem()) {
            this.currentScrollOffset = 0;
        }
    }

    // region Rendering Overrides

    /**
     * Replaces vanilla renderBg. Draws the custom background, slot buttons, and XP bar.
     */
    @Override
    protected void renderBg(GuiGraphics context, float partialTick, int mouseX, int mouseY) {
        int alignX = this.leftPos;
        int alignY = this.topPos;
        context.blit(BACKGROUND_TEXTURE, alignX, alignY, 0, 0, this.imageWidth, this.imageHeight);

        // Render dynamic ghost items for empty table slots
        if (!this.menu.getSlot(0).hasItem()) {
            context.blit(EMPTY_SLOT_TARGET_TEXTURE, alignX + this.menu.getSlot(0).x, alignY + this.menu.getSlot(0).y, 0, 0, 16, 16, 16, 16);
        }
        if (!this.menu.getSlot(1).hasItem()) {
            context.blit(EMPTY_SLOT_FUEL_TEXTURE, alignX + this.menu.getSlot(1).x, alignY + this.menu.getSlot(1).y, 0, 0, 16, 16, 16, 16);
        }
        if (!this.menu.getSlot(2).hasItem()) {
            context.blit(EMPTY_SLOT_SOURCE_TEXTURE, alignX + this.menu.getSlot(2).x, alignY + this.menu.getSlot(2).y, 0, 0, 16, 16, 16, 16);
        }

        boolean usePlain = Config.BINARY_ACCESSIBILITY_USE_PLAIN_BACKGROUND.get();

        EnchantmentNames.getInstance().initSeed(this.menu.getEnchantmentSeed());
        int lapisCount = this.menu.getSlot(1).getItem().getCount();

        int totalEnchants = this.menu.getEnchantmentListLength();
        int maxOffset = Math.max(0, totalEnchants - 1);
        if (this.currentScrollOffset > maxOffset) this.currentScrollOffset = maxOffset;
        if (this.currentScrollOffset < 0) this.currentScrollOffset = 0;

        for (int visualIndex = 0; visualIndex < PAGE_SIZE; ++visualIndex) {
            int realIndex = this.currentScrollOffset + visualIndex;
            int buttonX = alignX + ENCHANTING_BUTTON_X_OFFSET;
            int buttonY = alignY + ENCHANTING_BUTTON_Y_OFFSET + ENCHANTING_BUTTON_HEIGHT * visualIndex;

            if (realIndex >= totalEnchants) {
                int defaultTexIndex = realIndex % 10;
                this.drawEmptySlot(context, buttonX, buttonY, defaultTexIndex, usePlain);
                continue;
            }

            int enchantingPower = this.menu.costs[realIndex];
            int sourceId = this.menu.getEnchantmentSourceArray()[realIndex];
            EnchantmentSource sourceEnum = EnchantmentSource.byId(sourceId); // Resolve Enum here

            int id = this.menu.enchantClue[realIndex];
            int level = this.menu.levelClue[realIndex];

            // Resolve Texture Index based on source
            int texIndex;
            switch (sourceEnum) {
                case TARGET -> texIndex = this.menu.getTargetTextureIndices()[realIndex];
                case SOURCE -> texIndex = this.menu.getSourceTextureIndices()[realIndex];
                case TABLE -> texIndex = this.menu.getTableTextureIndices()[realIndex];
                default -> texIndex = realIndex % 10;
            }

            Holder<Enchantment> enchantment = null;
            if (id >= 0 && this.minecraft.level != null) {
                IdMap<Holder<Enchantment>> idMap = this.minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
                enchantment = idMap.byId(id);
            }

            if (enchantingPower <= 0 || enchantment == null) {
                this.drawEmptySlot(context, buttonX, buttonY, texIndex, usePlain);
            } else {
                switch (sourceEnum) {
                    case TARGET -> this.drawUpgradeSlot(context, buttonX, buttonY, mouseX, mouseY, lapisCount, enchantingPower, enchantment, level, texIndex, usePlain);
                    case SOURCE -> this.drawTransferSlot(context, buttonX, buttonY, mouseX, mouseY, lapisCount, enchantingPower, enchantment, level, texIndex, usePlain);
                    case TABLE -> this.drawApplySlot(context, buttonX, buttonY, mouseX, mouseY, lapisCount, enchantingPower, enchantment, level, texIndex, usePlain);
                    default -> this.drawEmptySlot(context, buttonX, buttonY, texIndex, usePlain);
                }
            }
        }

        this.drawRerollButton(context, alignX, alignY, mouseX, mouseY, lapisCount, this.menu.getEnchantmentSourceArray());
        this.drawNavButtons(context, alignX, alignY, mouseX, mouseY, totalEnchants);
        this.drawExperienceBar(context, alignX, alignY);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY + SCREEN_HEIGHT_ADJUSTMENT, 4210752, false);
    }

    /**
     * Replaces vanilla render. Handles standard drawing and custom tooltips.
     */
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        super.render(context, mouseX, mouseY, partialTick);
        this.renderTooltip(context, mouseX, mouseY);

        boolean isCreative = this.minecraft.player.isCreative();
        int lapisCount = this.menu.getSlot(1).getItem().getCount();

        boolean tooltipDrawn = this.drawEnchantmentSlotTooltips(context, mouseX, mouseY, lapisCount, isCreative);

        if (!tooltipDrawn) {
            this.drawRerollButtonTooltip(context, mouseX, mouseY, lapisCount, isCreative);
        }
    }

    /**
     * Draws the Previous and Next page buttons using the specific coordinates provided.
     * Shows disabled textures if navigation is not possible.
     */
    private void drawNavButtons(GuiGraphics context, int alignX, int alignY, int mouseX, int mouseY, int totalEnchants) {
        // Even if only 1 page, we might want to show disabled buttons for consistency,
        // or hide them. The previous code hid them.
        // If you want them always visible but disabled, remove this line.
        if (totalEnchants <= PAGE_SIZE) return;

        RenderSystem.enableBlend();

        boolean hasTargetItem = this.menu.getSlot(0).hasItem();

        // --- Backward Button (<) ---
        boolean canGoBack = canNavigate(hasTargetItem, totalEnchants, -1);
        int prevX = alignX + BACKWARD_X_OFFSET;
        int prevY = alignY + BACKWARD_Y_OFFSET;

        ResourceLocation prevTex = PAGE_BACKWARD_DISABLED_TEXTURE;
        if (canGoBack) {
            if (this.isHovering(BACKWARD_X_OFFSET, BACKWARD_Y_OFFSET, BACKWARD_BUTTON_WIDTH, BACKWARD_BUTTON_HEIGHT, mouseX, mouseY)) {
                prevTex = PAGE_BACKWARD_HIGHLIGHTED_TEXTURE;
            } else {
                prevTex = PAGE_BACKWARD_TEXTURE;
            }
        }
        context.blit(prevTex, prevX, prevY, 0, 0, BACKWARD_BUTTON_WIDTH, BACKWARD_BUTTON_HEIGHT, BACKWARD_BUTTON_WIDTH, BACKWARD_BUTTON_HEIGHT);

        // --- Forward Button (>) ---
        boolean canGoForward = canNavigate(hasTargetItem, totalEnchants, 1);
        int nextX = alignX + FORWARD_X_OFFSET;
        int nextY = alignY + FORWARD_Y_OFFSET;

        ResourceLocation nextTex = PAGE_FORWARD_DISABLED_TEXTURE;
        if (canGoForward) {
            if (this.isHovering(FORWARD_X_OFFSET, FORWARD_Y_OFFSET, FORWARD_BUTTON_WIDTH, FORWARD_BUTTON_HEIGHT, mouseX, mouseY)) {
                nextTex = PAGE_FORWARD_HIGHLIGHTED_TEXTURE;
            } else {
                nextTex = PAGE_FORWARD_TEXTURE;
            }
        }
        context.blit(nextTex, nextX, nextY, 0, 0, FORWARD_BUTTON_WIDTH, FORWARD_BUTTON_HEIGHT, FORWARD_BUTTON_WIDTH, FORWARD_BUTTON_HEIGHT);

        RenderSystem.disableBlend();
    }
    // endregion

    // region Input Handling

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int alignX = this.leftPos;
        int alignY = this.topPos;

        int clickedButtonIndex = -1;

        // Check reroll button (0)
        double mouseXdiff = mouseX - (double) (alignX + REROLL_BUTTON_X_OFFSET);
        double mouseYdiff = mouseY - (double) (alignY + REROLL_BUTTON_Y_OFFSET);
        if (mouseXdiff >= 0.0F && mouseYdiff >= 0.0F && mouseXdiff < (double) REROLL_BUTTON_WIDTH && mouseYdiff < (double) REROLL_BUTTON_HEIGHT) {
            clickedButtonIndex = REROLL_BUTTON_ID;
        }

        // 2. Navigation Buttons
        int totalEnchants = this.menu.getEnchantmentListLength();
        boolean hasTargetItem = this.menu.getSlot(0).hasItem();

        if (totalEnchants > PAGE_SIZE) {
            // Backward Click
            if (this.isHovering(BACKWARD_X_OFFSET, BACKWARD_Y_OFFSET, BACKWARD_BUTTON_WIDTH, BACKWARD_BUTTON_HEIGHT, mouseX, mouseY)) {
                if (navigatePage(hasTargetItem, totalEnchants, -1)) return true;
            }
            // Forward Click
            if (this.isHovering(FORWARD_X_OFFSET, FORWARD_Y_OFFSET, FORWARD_BUTTON_WIDTH, FORWARD_BUTTON_HEIGHT, mouseX, mouseY)) {
                if (navigatePage(hasTargetItem, totalEnchants, 1)) return true;
            }
        }

        // Check enchantment buttons (1-3)
        if (clickedButtonIndex == -1) {
            for (int i = 0; i < PAGE_SIZE; ++i) {
                double mouseDiffX = mouseX - (double) (alignX + ENCHANTING_BUTTON_X_OFFSET);
                double mouseDiffY = mouseY - (double) (alignY + ENCHANTING_BUTTON_Y_OFFSET + ENCHANTING_BUTTON_HEIGHT * i);

                if (mouseDiffX >= 0.0F && mouseDiffY >= 0.0F && mouseDiffX < ENCHANTING_BUTTON_WIDTH && mouseDiffY < ENCHANTING_BUTTON_HEIGHT) {
                    clickedButtonIndex = i + 1;
                    break;
                }
            }
        }

        if (clickedButtonIndex != -1) {
            int serverButtonId = clickedButtonIndex;
            if (clickedButtonIndex != REROLL_BUTTON_ID) {

                serverButtonId = clickedButtonIndex + this.currentScrollOffset;
            }

            // Bounds Check using raw length
            if (serverButtonId != REROLL_BUTTON_ID && (serverButtonId - 1) >= totalEnchants) {
                return super.mouseClicked(mouseX, mouseY, button);
            }

            if (this.menu.clickMenuButton(this.minecraft.player, serverButtonId)) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, serverButtonId);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int totalEnchants = this.menu.getEnchantmentListLength();
        boolean hasTargetItem = this.menu.getSlot(0).hasItem();

        if (totalEnchants > PAGE_SIZE) {
            // Scroll Up (Positive) -> Go to Previous Offset
            if (verticalAmount > 0) {
                if (navigatePage(hasTargetItem, totalEnchants, -1)) return true;
            }

            // Scroll Down (Negative) -> Go to Next Offset
            if (verticalAmount < 0) {
                if (navigatePage(hasTargetItem, totalEnchants, 1)) return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    // endregion

    // region Logic Helpers

    /**
     * Checks if navigation is possible in the given direction.
     * @param direction -1 for backward, 1 for forward.
     */
    private boolean canNavigate(boolean hasTargetItem, int totalEnchants, int direction) {
        if (!hasTargetItem) return false;

        if (direction < 0) {
            return this.currentScrollOffset > 0;
        } else {
            return this.currentScrollOffset + PAGE_SIZE < totalEnchants;
        }
    }

    /**
     * Attempts to navigate in the given direction. Plays sound if successful.
     * @param direction -1 for backward, 1 for forward.
     * @return true if navigation occurred.
     */
    private boolean navigatePage(boolean hasTargetItem, int totalEnchants, int direction) {
        if (canNavigate(hasTargetItem, totalEnchants, direction)) {
            int step = Config.BOUNDED_SCROLL_STEP.get();

            if (direction > 0) {
                // Scrolling Down
                int maxOffset = Math.max(0, totalEnchants - 1);
                this.currentScrollOffset = Math.min(this.currentScrollOffset + step, maxOffset);
            } else {
                // Scrolling Up
                this.currentScrollOffset = Math.max(this.currentScrollOffset - step, 0);
            }

            this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            return true;
        }
        return false;
    }
    // endregion

    // region Helper Methods (Drawing & Logic)

    private void drawEmptySlot(GuiGraphics context, int buttonX, int buttonY, int texIndex, boolean usePlain) {
        RenderSystem.enableBlend();
        ResourceLocation disabledTex = usePlain ? EnchantmentSource.TABLE.disabledTexture
                : EnchantmentSource.TABLE.getDisabledTexture(texIndex);
        context.blit(disabledTex, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);
        RenderSystem.disableBlend();
    }

    private void drawUpgradeSlot(
            GuiGraphics context,
            int buttonX,
            int buttonY,
            int mouseX,
            int mouseY,
            int lapisCount,
            int enchantingPower,
            Holder<Enchantment> enchantment,
            int enchantmentLevel,
            int texIndex,
            boolean usePlain
    ) {
        boolean isMaxed = (enchantmentLevel >= enchantment.value().getMaxLevel());

        if (isMaxed) {
            this.drawMaxedSlot(context, enchantment, enchantmentLevel, buttonX, buttonY);
        } else {
            int cost = this.menu.calculateEnchantmentCost(enchantment.value());
            boolean affordable = (lapisCount >= cost && this.minecraft.player.experienceLevel >= enchantingPower && this.minecraft.player.experienceLevel >= cost) || this.minecraft.player.isCreative();

            // Delegate to generic drawer using TARGET enum
            this.drawEnchantmentSlot(context, buttonX, buttonY, mouseX, mouseY, enchantingPower, cost, affordable, EnchantmentSource.TARGET, texIndex, usePlain, Enchantment.getFullname(enchantment, enchantmentLevel));
        }
    }

    private void drawTransferSlot(
            GuiGraphics context,
            int buttonX,
            int buttonY,
            int mouseX,
            int mouseY,
            int lapisCount,
            int enchantingPower,
            Holder<Enchantment> enchantment,
            int enchantmentLevel,
            int texIndex,
            boolean usePlain
    ) {
        int cost = this.menu.calculateEnchantmentCost(enchantment.value());
        boolean affordable = (lapisCount >= cost && this.minecraft.player.experienceLevel >= enchantingPower && this.minecraft.player.experienceLevel >= cost) || this.minecraft.player.isCreative();

        // Delegate to generic drawer using SOURCE enum
        this.drawEnchantmentSlot(context, buttonX, buttonY, mouseX, mouseY, enchantingPower, cost, affordable, EnchantmentSource.SOURCE, texIndex, usePlain, Enchantment.getFullname(enchantment, enchantmentLevel));
    }

    private void drawApplySlot(
            GuiGraphics context,
            int buttonX,
            int buttonY,
            int mouseX,
            int mouseY,
            int lapisCount,
            int enchantingPower,
            Holder<Enchantment> enchantment,
            int enchantmentLevel,
            int texIndex,
            boolean usePlain
    ) {
        int cost = this.menu.calculateEnchantmentCost(enchantment.value());
        boolean affordable = (lapisCount >= cost && this.minecraft.player.experienceLevel >= enchantingPower && this.minecraft.player.experienceLevel >= cost) || this.minecraft.player.isCreative();

        // Handle text obfuscation specific to Table enchantments
        Component originalName = Enchantment.getFullname(enchantment, enchantmentLevel);
        Style galacticStyle = originalName.getStyle().withFont(GALACTIC_FONT_ID);
        Component galacticName = originalName.copy().setStyle(galacticStyle);

        Component name = Config.BINARY_ACCESSIBILITY_OBFUSCATE_NEW_ENCHANTMENTS.get() ? galacticName : originalName;

        // Delegate to generic drawer using TABLE enum
        this.drawEnchantmentSlot(context, buttonX, buttonY, mouseX, mouseY, enchantingPower, cost, affordable, EnchantmentSource.TABLE, texIndex, usePlain, name);
    }

    private void drawEnchantmentSlot(
            GuiGraphics context,
            int buttonX,
            int buttonY,
            int mouseX,
            int mouseY,
            int enchantingPower,
            int cost,
            boolean affordable,
            EnchantmentSource source,
            int texIndex,
            boolean usePlain,
            Component enchantmentName
    ) {
        int costIndex = Math.max(0, cost - 1);
        int textX = buttonX + ENCHANTING_TEXT_X_OFFSET;
        int textY = buttonY + ENCHANTING_TEXT_Y_OFFSET;
        String string = "" + enchantingPower;

        // --- 1. Determine State and Target Colors ---
        int targetTextColor;
        int powerColor;
        boolean hovering = false;

        if (!affordable) {
            targetTextColor = source.disabledColor;
            powerColor = ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR;
        } else {
            int mouseDiffX = mouseX - buttonX;
            int mouseDiffY = mouseY - buttonY;
            hovering = mouseDiffX >= 0 && mouseDiffY >= 0 && mouseDiffX < ENCHANTING_BUTTON_WIDTH && mouseDiffY < ENCHANTING_BUTTON_HEIGHT;

            targetTextColor = hovering ? source.highlightedColor : source.enabledColor;
            powerColor = ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR;
        }

        // --- 2. Resolve Textures from Enum ---
        ResourceLocation enabledTex = usePlain ? source.enabledTexture : source.getEnabledTexture(texIndex);
        ResourceLocation highlightedTex = usePlain ? source.highlightedTexture : source.getHighlightedTexture(texIndex);
        ResourceLocation disabledTex = usePlain ? source.disabledTexture : source.getDisabledTexture(texIndex);

        // --- 3. Render Background Textures ---
        RenderSystem.enableBlend();
        if (!affordable) {
            context.blit(disabledTex, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);
            context.blitSprite(DISABLED_LEVEL_SPRITES[costIndex], buttonX + ENCHANTING_COST_X_OFFSET, buttonY + ENCHANTING_COST_Y_OFFSET, ENCHANTING_COST_WIDTH, ENCHANTING_COST_HEIGHT);
        } else {
            ResourceLocation textureToDraw = hovering ? highlightedTex : enabledTex;
            context.blit(textureToDraw, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);
            context.blitSprite(ENABLED_LEVEL_SPRITES[costIndex], buttonX + ENCHANTING_COST_X_OFFSET, buttonY + ENCHANTING_COST_Y_OFFSET, ENCHANTING_COST_WIDTH, ENCHANTING_COST_HEIGHT);
        }
        RenderSystem.disableBlend();

        // --- 4. Prepare Text Component (Apply Overrides) ---
        MutableComponent displayComponent = enchantmentName.copy();

        if (Config.BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_NAME_COLOR.get()) {
            displayComponent.withStyle(style -> style.withColor(targetTextColor));
        }

        if (Config.BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_LEVEL_COLOR.get()) {
            for (Component sibling : displayComponent.getSiblings()) {
                if (sibling instanceof MutableComponent mutableSibling) {
                    mutableSibling.withStyle(style -> style.withColor(targetTextColor));
                }
            }
        }

        // --- 5. Render Text ---
        context.drawWordWrap(this.font, displayComponent, textX, textY, ENCHANTING_TEXT_MAX_WIDTH, targetTextColor);

        // --- 6. Render Power Requirement Number (Outline) ---
        int outlineColor = 0;
        context.drawString(this.font, string, textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string) + 1, buttonY + ENCHANTING_POWER_Y_OFFSET, outlineColor, false);
        context.drawString(this.font, string, textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string) - 1, buttonY + ENCHANTING_POWER_Y_OFFSET, outlineColor, false);
        context.drawString(this.font, string, textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string), buttonY + ENCHANTING_POWER_Y_OFFSET + 1, outlineColor, false);
        context.drawString(this.font, string, textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string), buttonY + ENCHANTING_POWER_Y_OFFSET - 1, outlineColor, false);
        context.drawString(this.font, string, textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string), buttonY + ENCHANTING_POWER_Y_OFFSET, powerColor, false);
        context.drawString(this.font, string, textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string), buttonY + ENCHANTING_POWER_Y_OFFSET, powerColor, true);
    }

    private void drawMaxedSlot(GuiGraphics context, Holder<Enchantment> enchantment, int enchantmentLevel, int buttonX, int buttonY) {
        // 1. Determine State
        boolean isOverEnchanted = enchantmentLevel > enchantment.value().getMaxLevel();

        ResourceLocation backgroundTexture = isOverEnchanted ? ENCHANTMENT_SLOT_OVER_ENCHANTED_TEXTURE : ENCHANTMENT_SLOT_MAXED_OUT_TEXTURE;
        int targetColor = isOverEnchanted ? ENCHANTMENT_OVER_ENCHANTED_TEXT_COLOR : ENCHANTMENT_MAXED_OUT_TEXT_COLOR;

        // 2. Render Background
        context.blit(backgroundTexture, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);

        // 3. Prepare Text Component (Apply Overrides)
        Component originalName = Enchantment.getFullname(enchantment, enchantmentLevel);
        MutableComponent displayComponent = originalName.copy();

        // A. Apply Name Override (Root)
        // Forces the main name to match the specific "Maxed Out" or "Over Enchanted" color
        if (Config.BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_NAME_COLOR.get()) {
            displayComponent.withStyle(style -> style.withColor(targetColor));
        }

        // B. Apply Level Override (Siblings)
        // Forces the level (e.g. " V") to match the specific color
        if (Config.BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_LEVEL_COLOR.get()) {
            for (Component sibling : displayComponent.getSiblings()) {
                if (sibling instanceof MutableComponent mutableSibling) {
                    mutableSibling.withStyle(style -> style.withColor(targetColor));
                }
            }
        }

        // 4. Render Text
        int textX = buttonX + 8;
        int textY = buttonY + ENCHANTING_TEXT_Y_OFFSET;
        context.drawString(this.font, displayComponent, textX, textY, targetColor, false);
    }

    private void drawRerollButton(GuiGraphics context, int alignX, int alignY, int mouseX, int mouseY, int lapisCount, int[] enchantmentSources) {
        ItemStack target = this.menu.getEnchantmentTarget();
        boolean targetIsEmpty = target.isEmpty();
        boolean targetIsEnchantable = !targetIsEmpty && (target.is(Items.BOOK) || target.isEnchantable());
        boolean targetIsSourceEnchantable = Arrays.stream(enchantmentSources).anyMatch(element -> element == EnchantmentSource.SOURCE.getId());

        ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
        int occupiedSlots = EnchantmentLib.getEnchantments(curseFreeTarget).size();

        int rerollCost = occupiedSlots + 1;
        int costIndex = Math.max(0, rerollCost - 1);
        boolean hasTableSource = Arrays.stream(enchantmentSources).anyMatch(source -> source == EnchantmentSource.TABLE.getId());
        boolean canReroll = occupiedSlots < 3 && !targetIsSourceEnchantable && targetIsEnchantable && hasTableSource;

        int x = alignX + REROLL_BUTTON_X_OFFSET;
        int y = alignY + REROLL_BUTTON_Y_OFFSET;

        RenderSystem.enableBlend();
        boolean cannotAfford = (lapisCount < rerollCost || this.minecraft.player.experienceLevel < rerollCost) && !this.minecraft.player.isCreative();
        boolean rerollEnabled = !targetIsEmpty && targetIsEnchantable && canReroll && !cannotAfford;

        if (rerollEnabled) {
            int mouseXdiff = mouseX - x;
            int mouseYdiff = mouseY - y;

            if (mouseXdiff >= 0 && mouseYdiff >= 0 && mouseXdiff < REROLL_BUTTON_WIDTH && mouseYdiff < REROLL_BUTTON_HEIGHT) {
                context.blit(REROLL_HIGHLIGHTED_TEXTURE, x, y, 0, 0, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT);
            } else {
                context.blit(REROLL_TEXTURE, x, y, 0, 0, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT);
            }
            if (costIndex < ENABLED_LEVEL_SPRITES.length) {
                context.blitSprite(ENABLED_LEVEL_SPRITES[costIndex], x + REROLL_COST_X_OFFSET, y + REROLL_COST_Y_OFFSET, REROLL_COST_WIDTH, REROLL_COST_HEIGHT);
            }
        } else {
            context.blit(REROLL_DISABLED_TEXTURE, x, y, 0, 0, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT);
            boolean showDisabledCost = !targetIsEmpty && targetIsEnchantable && canReroll;
            if (showDisabledCost && costIndex < DISABLED_LEVEL_SPRITES.length) {
                context.blitSprite(DISABLED_LEVEL_SPRITES[costIndex], x + REROLL_COST_X_OFFSET, y + REROLL_COST_Y_OFFSET, REROLL_COST_WIDTH, REROLL_COST_HEIGHT);
            }
        }
        RenderSystem.disableBlend();
    }

    private void drawExperienceBar(GuiGraphics context, int alignX, int alignY) {
        int barX = alignX + EXPERIENCE_BAR_X_OFFSET;
        int barY = alignY + EXPERIENCE_BAR_Y_OFFSET;
        int barWidth = EXPERIENCE_BAR_WIDTH;
        int barHeight = EXPERIENCE_BAR_HEIGHT;

        context.blit(EXPERIENCE_BAR_INACTIVE, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        float progress = this.minecraft.player.experienceProgress;
        int activeWidth = (int) (progress * (float) barWidth);

        if (activeWidth > 0) {
            context.blit(EXPERIENCE_BAR_ACTIVE, barX, barY, 0, 0, activeWidth, barHeight, barWidth, barHeight);
        }

        int playerLevel = this.minecraft.player.experienceLevel;
        if (playerLevel > 0) {
            String levelString = "" + playerLevel;
            int textX = barX + (barWidth / 2) - (this.font.width(levelString) / 2);
            int textY = alignY + EXPERIENCE_BAR_Y_OFFSET - 6;
            int outlineColor = 0;
            context.drawString(this.font, levelString, textX + 1, textY, outlineColor, false);
            context.drawString(this.font, levelString, textX - 1, textY, outlineColor, false);
            context.drawString(this.font, levelString, textX, textY + 1, outlineColor, false);
            context.drawString(this.font, levelString, textX, textY - 1, outlineColor, false);
            context.drawString(this.font, levelString, textX, textY, ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR, false);
        }
    }

    private String getSafeDescriptionKey(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey().map(key -> Util.makeDescriptionId("enchantment", key.location()) + ".desc").orElse(null);
    }

    private boolean drawEnchantmentSlotTooltips(GuiGraphics context, int mouseX, int mouseY, int lapisCount, boolean isCreative) {
        int totalEnchants = this.menu.costs.length;

        for (int visualIndex = 0; visualIndex < PAGE_SIZE; ++visualIndex) {
            int realIndex = this.currentScrollOffset + visualIndex;

            if (realIndex >= totalEnchants) break;

            int powerRequirement = this.menu.costs[realIndex];
            int id = this.menu.enchantClue[realIndex];

            Holder<Enchantment> enchantment = null;
            if (id >= 0 && this.minecraft.level != null) {
                IdMap<Holder<Enchantment>> idMap = this.minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
                enchantment = idMap.byId(id);
            }

            int enchantmentLevel = this.menu.levelClue[realIndex];

            int source = this.menu.getEnchantmentSourceArray()[realIndex];

            int buttonY = ENCHANTING_BUTTON_Y_OFFSET + (ENCHANTING_BUTTON_HEIGHT * visualIndex);

            if (this.isHovering(ENCHANTING_BUTTON_X_OFFSET, buttonY, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT, mouseX, mouseY)
                    && powerRequirement > 0 && enchantmentLevel >= 0 && enchantment != null) {

                boolean isMaxed = (source == EnchantmentSource.TARGET.getId() && enchantmentLevel >= enchantment.value().getMaxLevel());

                if (isMaxed) {
                    this.drawMaxedTooltip(context, mouseX, mouseY, enchantment, enchantmentLevel);
                    return true;
                } else if (source == EnchantmentSource.TARGET.getId()) {
                    this.drawUpgradeTooltip(context, mouseX, mouseY, lapisCount, isCreative, enchantment, enchantmentLevel, powerRequirement);
                    return true;
                } else if (source == EnchantmentSource.SOURCE.getId()) {
                    this.drawTransferTooltip(context, mouseX, mouseY, lapisCount, isCreative, enchantment, enchantmentLevel, powerRequirement);
                    return true;
                } else if (source == EnchantmentSource.TABLE.getId()) {
                    this.drawApplyTooltip(context, mouseX, mouseY, lapisCount, isCreative, enchantment, enchantmentLevel, powerRequirement);
                    return true;
                }
            }
        }
        return false;
    }

    private void drawMaxedTooltip(GuiGraphics context, int mouseX, int mouseY, Holder<Enchantment> enchantment, int enchantmentLevel) {
        List<FormattedCharSequence> list = new ArrayList<>();
        list.add(Enchantment.getFullname(enchantment, enchantmentLevel).copy().getVisualOrderText());
        String descKey = getSafeDescriptionKey(enchantment);
        if (descKey != null && Config.BINARY_SHOW_TABLE_ENCHANTMENT_DESCRIPTIONS.get()) {
            Component description = Component.translatable(descKey).withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR.get());
            list.addAll(EnchantmentLib.wrapDescription(description));
        }
        context.renderTooltip(this.font, list, mouseX, mouseY);
    }

    private void drawUpgradeTooltip(GuiGraphics context, int mouseX, int mouseY, int lapisCount, boolean isCreative, Holder<Enchantment> enchantment, int enchantmentLevel, int powerRequirement) {
        int cost = this.menu.calculateEnchantmentCost(enchantment.value());
        List<FormattedCharSequence> list = new ArrayList<>();
        list.add(Component.translatable("gui.enchanting_overhauled.upgrade").withStyle(ChatFormatting.WHITE).getVisualOrderText());
        list.add(Enchantment.getFullname(enchantment, enchantmentLevel).copy().getVisualOrderText());
        String descKey = getSafeDescriptionKey(enchantment);
        if (descKey != null && Config.BINARY_SHOW_TABLE_ENCHANTMENT_DESCRIPTIONS.get()) {
            Component description = Component.translatable(descKey).withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR.get());
            list.addAll(EnchantmentLib.wrapDescription(description));
        }
        this.drawTooltipCost(list, isCreative, powerRequirement, cost, lapisCount);
        context.renderTooltip(this.font, list, mouseX, mouseY);
    }

    private void drawTransferTooltip(GuiGraphics context, int mouseX, int mouseY, int lapisCount, boolean isCreative, Holder<Enchantment> enchantment, int enchantmentLevel, int powerRequirement) {
        int cost = this.menu.calculateEnchantmentCost(enchantment.value());
        List<FormattedCharSequence> list = new ArrayList<>();
        list.add(Component.translatable("gui.enchanting_overhauled.transfer").withStyle(ChatFormatting.WHITE).getVisualOrderText());
        list.add(Enchantment.getFullname(enchantment, enchantmentLevel).copy().getVisualOrderText());
        String descKey = getSafeDescriptionKey(enchantment);
        if (descKey != null && Config.BINARY_SHOW_TABLE_ENCHANTMENT_DESCRIPTIONS.get()) {
            Component description = Component.translatable(descKey).withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR.get());
            list.addAll(EnchantmentLib.wrapDescription(description));
        }
        this.drawTooltipCost(list, isCreative, powerRequirement, cost, lapisCount);
        context.renderTooltip(this.font, list, mouseX, mouseY);
    }

    private void drawApplyTooltip(GuiGraphics context, int mouseX, int mouseY, int lapisCount, boolean isCreative, Holder<Enchantment> enchantment, int enchantmentLevel, int powerRequirement) {
        int cost = this.menu.calculateEnchantmentCost(enchantment.value());
        List<FormattedCharSequence> list = new ArrayList<>();

        ResourceKey<EnchantmentTheme> themeKey = EnchantmentLib.getThemeKey(this.minecraft.level.registryAccess(), enchantment);
        Optional<Registry<EnchantmentTheme>> registryOpt = Services.PLATFORM.getThemeRegistry(this.minecraft.level.registryAccess());
        int color = 0xFFFFFF;
        if (registryOpt.isPresent()) {
            EnchantmentTheme theme = registryOpt.get().get(themeKey);
            if (theme != null) {
                color = theme.colorCode().orElse(0xFFFFFF);
            }
        }
        final int immutableColor = color;

        MutableComponent enchantmentName = (MutableComponent) Enchantment.getFullname(enchantment, enchantmentLevel);
        String descKey = getSafeDescriptionKey(enchantment);
        MutableComponent description = (descKey != null) ? Component.translatable(descKey).withStyle(ChatFormatting.GRAY) : Component.empty();
        Component title = Component.translatable("gui.enchanting_overhauled.apply").withStyle(ChatFormatting.WHITE);

        if (Config.BINARY_ACCESSIBILITY_OBFUSCATE_NEW_ENCHANTMENTS.get()) {
            enchantmentName = enchantmentName.setStyle(enchantmentName.getStyle().withFont(GALACTIC_FONT_ID)).withStyle(style -> style.withColor(immutableColor));
            if (descKey != null) {
                description = description.setStyle(description.getStyle().withFont(GALACTIC_FONT_ID)).withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR.get());
            }
        }

        list.add(title.getVisualOrderText());
        list.add(enchantmentName.getVisualOrderText());
        if (descKey != null && Config.BINARY_SHOW_TABLE_ENCHANTMENT_DESCRIPTIONS.get()) {
            list.addAll(EnchantmentLib.wrapDescription(description));
        }
        this.drawTooltipCost(list, isCreative, powerRequirement, cost, lapisCount);
        context.renderTooltip(this.font, list, mouseX, mouseY);
    }

    private void drawTooltipCost(List<FormattedCharSequence> list, boolean isCreative, int powerRequirement, int cost, int lapisCount) {
        if (!isCreative) {
            list.add(FormattedCharSequence.EMPTY);
            if (this.minecraft.player.experienceLevel < powerRequirement) {
                list.add(Component.translatable("container.enchant.level.requirement", powerRequirement).withStyle(ChatFormatting.RED).getVisualOrderText());
            } else {
                MutableComponent mutableText = (cost == 1) ? Component.translatable("container.enchant.lapis.one") : Component.translatable("container.enchant.lapis.many", cost);
                list.add(mutableText.withStyle(lapisCount >= cost ? ChatFormatting.GRAY : ChatFormatting.RED).getVisualOrderText());

                MutableComponent mutableText2 = (cost == 1) ? Component.translatable("container.enchant.level.one") : Component.translatable("container.enchant.level.many", cost);
                list.add(mutableText2.withStyle(ChatFormatting.GRAY).getVisualOrderText());
            }
        }
    }

    private void drawRerollButtonTooltip(GuiGraphics context, int mouseX, int mouseY, int lapisCount, boolean isCreative) {
        if (this.isHovering(REROLL_BUTTON_X_OFFSET, REROLL_BUTTON_Y_OFFSET, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT, mouseX, mouseY)) {
            int[] enchantmentSources = this.menu.getEnchantmentSourceArray();
            ItemStack target = this.menu.getEnchantmentTarget();
            boolean targetIsEmpty = target.isEmpty();
            boolean targetIsEnchantable = !targetIsEmpty && (target.is(Items.BOOK) || target.isEnchantable());
            boolean targetIsSourceEnchantable = Arrays.stream(enchantmentSources).anyMatch(element -> element == EnchantmentSource.SOURCE.getId());
            ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
            int occupiedSlots = EnchantmentLib.getEnchantments(curseFreeTarget).size();
            int rerollCost = occupiedSlots + 1;
            boolean canReroll = occupiedSlots < 3 && !targetIsSourceEnchantable && targetIsEnchantable;

            List<FormattedCharSequence> list = new ArrayList<>();
            list.add(Component.translatable("gui.enchanting_overhauled.turn_page").withStyle(ChatFormatting.WHITE).getVisualOrderText());

            if (!targetIsEmpty && canReroll) {
                this.drawTooltipCost(list, isCreative, rerollCost, rerollCost, lapisCount);
            }
            context.renderTooltip(this.font, list, mouseX, mouseY);
        }
    }

    // endregion
}