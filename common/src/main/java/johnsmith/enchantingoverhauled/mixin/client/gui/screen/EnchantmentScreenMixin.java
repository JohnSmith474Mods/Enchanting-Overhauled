package johnsmith.enchantingoverhauled.mixin.client.gui.screen;


import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.accessor.EnchantmentMenuAccessor;
import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;

import com.mojang.blaze3d.systems.RenderSystem;

import johnsmith.enchantingoverhauled.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * Mixin to {@link EnchantmentScreen} to implement the client-side visuals for the
 * enchanting overhaul.
 * <p>
 * This mixin:
 * <ul>
 * <li>Replaces the background texture with a taller version.</li>
 * <li>Completely overrides the background drawing logic
 * ({@link #drawCustomBackground}) to
 * render the new enchantment/upgrade/transfer slots and the reroll button.</li>
 * <li>Implements a custom experience bar ({@link #enchanting_Overhauled$drawExperienceBar}).</li>
 * <li>Overrides the mouse click handler ({@link #mouseClicked}) to correctly
 * process clicks on the new button layout.</li>
 * <li>Overrides the render method ({@link #overrideRender}) to provide custom
 * tooltips for all buttons and fix enchantment name rendering.</li>
 * </ul>
 */
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {

    // region Font Identifiers
    @Unique
    private static final ResourceLocation GALACTIC_FONT_ID = ResourceLocation.fromNamespaceAndPath("minecraft", "alt");
    // endregion

    // region Texture Identifiers
    /** The main background texture for the new 176x186 GUI. */
    @Unique
    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/background.png");
    /** Texture for the "Turn Page" reroll button in its normal state. */
    @Unique
    private static final ResourceLocation REROLL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/enabled/reroll.png");
    /** Texture for the "Turn Page" reroll button when hovered. */
    @Unique
    private static final ResourceLocation REROLL_HIGHLIGHTED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/highlighted/reroll.png");
    /** Texture for the "Turn Page" reroll button when disabled. */
    @Unique
    private static final ResourceLocation REROLL_DISABLED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/disabled/reroll.png");

    /**
     * Texture for an enchantment slot when the enchantment is at its maximum level.
     */
    @Unique
    private static final ResourceLocation ENCHANTMENT_SLOT_MAXED_OUT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/maxed_out.png");

    /**
     * Texture for an enchantment slot when the enchantment is above its maximum
     * level.
     */
    @Unique
    private static final ResourceLocation ENCHANTMENT_SLOT_OVER_ENCHANTED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/over_enchanted.png");

    /** Texture for the filled (active) part of the player's experience bar. */
    @Unique
    private static final ResourceLocation EXPERIENCE_BAR_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/experience_bar/full.png");
    /**
     * Texture for the empty (inactive) background of the player's experience bar.
     */

    @Unique
    private static final ResourceLocation EXPERIENCE_BAR_INACTIVE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/experience_bar/empty.png");

    // ADDED FOR ACCESSIBILITY
    @Unique
    private static final ResourceLocation PLAIN_TARGET_ENABLED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/enabled/target_plain.png");
    @Unique
    private static final ResourceLocation PLAIN_TARGET_HIGHLIGHTED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/highlighted/target_plain.png");
    @Unique
    private static final ResourceLocation PLAIN_TARGET_DISABLED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/disabled/target_plain.png");

    @Unique
    private static final ResourceLocation PLAIN_SOURCE_ENABLED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/enabled/source_plain.png");
    @Unique
    private static final ResourceLocation PLAIN_SOURCE_HIGHLIGHTED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/highlighted/source_plain.png");
    @Unique
    private static final ResourceLocation PLAIN_SOURCE_DISABLED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/disabled/source_plain.png");

    @Unique
    private static final ResourceLocation PLAIN_TABLE_ENABLED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/enabled/table_plain.png");
    @Unique
    private static final ResourceLocation PLAIN_TABLE_HIGHLIGHTED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/highlighted/table_plain.png");
    @Unique
    private static final ResourceLocation PLAIN_TABLE_DISABLED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/disabled/table_plain.png");
    // endregion

    // region Screen Constants
    /** The original background height of the vanilla enchanting screen. */
    @Unique
    private static final int SCREEN_ORIGINAL_HEIGHT = 166;
    /** The number of pixels to add to the screen height to fit the new GUI. */
    @Unique
    private static final int SCREEN_HEIGHT_ADJUSTMENT = 29;
    // endregion

    // region Reroll Button Constants
    /**
     * The button ID used in {@link EnchantmentMenu#clickMenuButton} for the
     * reroll action.
     */
    @Unique
    private static final int REROLL_BUTTON_INDEX = 3;
    /** X-coordinate offset from the screen's left edge for the reroll button. */
    @Unique
    private static final int REROLL_BUTTON_X_OFFSET = 15 - 7;
    /** Y-coordinate offset from the screen's top edge for the reroll button. */
    @Unique
    private static final int REROLL_BUTTON_Y_OFFSET = 17;
    /** Width of the reroll button. */
    @Unique
    private static final int REROLL_BUTTON_WIDTH = 36;
    /** Height of the reroll button. */
    @Unique
    private static final int REROLL_BUTTON_HEIGHT = 18;
    /** Width of the cost icon on the reroll button. */
    @Unique
    private static final int REROLL_COST_WIDTH = 16;
    /** Height of the cost icon on the reroll button. */
    @Unique
    private static final int REROLL_COST_HEIGHT = 16;
    /** X-coordinate offset from the button's left edge for the cost icon. */
    @Unique
    private static final int REROLL_COST_X_OFFSET = -1;
    /** Y-coordinate offset from the button's top edge for the cost icon. */
    @Unique
    private static final int REROLL_COST_Y_OFFSET = 1;
    // endregion

    // region Enchantment Button Constants
    /** Width of the three main enchantment/upgrade/transfer buttons. */
    @Unique
    private static final int ENCHANTING_BUTTON_WIDTH = 108 + 7;
    /** Height of the three main enchantment/upgrade/transfer buttons. */
    @Unique
    private static final int ENCHANTING_BUTTON_HEIGHT = 18;
    /**
     * X-coordinate offset from the screen's left edge for the enchantment buttons.
     */
    @Unique
    private static final int ENCHANTING_BUTTON_X_OFFSET = 60 - 7 - 1;
    /**
     * Y-coordinate offset from the screen's top edge for the first enchantment
     * button.
     */
    @Unique
    private static final int ENCHANTING_BUTTON_Y_OFFSET = 17 + 2;
    /** Height of the cost icon on the enchantment buttons. */
    @Unique
    private static final int ENCHANTING_COST_HEIGHT = 16;
    /** Width of the cost icon on the enchantment buttons. */
    @Unique
    private static final int ENCHANTING_COST_WIDTH = 16;
    /** X-coordinate offset from the button's left edge for the cost icon. */
    @Unique
    private static final int ENCHANTING_COST_X_OFFSET = 0;
    /** Y-coordinate offset from the button's top edge for the cost icon. */
    @Unique
    private static final int ENCHANTING_COST_Y_OFFSET = 2;
    /** X-coordinate offset from the button's left edge for the power level text. */
    @Unique
    private static final int ENCHANTING_POWER_X_OFFSET = 86 + 11;
    /** Y-coordinate offset from the button's top edge for the power level text. */
    @Unique
    private static final int ENCHANTING_POWER_Y_OFFSET = 8;
    /** X-coordinate offset from the button's left edge for the enchantment text. */
    @Unique
    private static final int ENCHANTING_TEXT_X_OFFSET = ENCHANTING_COST_WIDTH;
    /** Y-coordinate offset from the button's top edge for the enchantment text. */
    @Unique
    private static final int ENCHANTING_TEXT_Y_OFFSET = 5;
    /** Max width of the enchantment name text before clipping. */
    @Unique
    private static final int ENCHANTING_TEXT_MAX_WIDTH = ENCHANTING_BUTTON_WIDTH - ENCHANTING_TEXT_X_OFFSET - 2;
    // endregion

    // region Experience Bar Constants
    /** X-coordinate offset from the screen's left edge for the experience bar. */
    @Unique
    private static final int EXPERIENCE_BAR_X_OFFSET = 60 - 1;
    /** Y-coordinate offset from the screen's top edge for the experience bar. */
    @Unique
    private static final int EXPERIENCE_BAR_Y_OFFSET = 80 + 5;
    /** Width of the experience bar. */
    @Unique
    private static final int EXPERIENCE_BAR_WIDTH = 102;
    /** Height of the experience bar. */
    @Unique
    private static final int EXPERIENCE_BAR_HEIGHT = 5;
    // endregion

    // region Tooltip Constants
    /** Max width in pixels for enchantment description tooltips before wrapping. */
    @Unique
    private static final int MAX_TOOLTIP_WIDTH = 170;
    // endregion

    // region Text Colors
    /** Text color for an enabled, table-generated enchantment. */
    @Unique
    private static final int ENCHANTING_FROM_TABLE_TEXT_ENABLED_COLOR = 0x000000;
    /** Text color for a hovered, table-generated enchantment. */
    @Unique
    private static final int ENCHANTING_FROM_TABLE_TEXT_HIGHLIGHTED_COLOR = 0x000000;
    /** Text color for a disabled, table-generated enchantment. */
    @Unique
    private static final int ENCHANTING_FROM_TABLE_TEXT_DISABLED_COLOR = 0xFFFFFF;

    /** Text color for an enabled, source-transferred enchantment. */
    @Unique
    private static final int ENCHANTING_FROM_SOURCE_TEXT_ENABLED_COLOR = 0x000000;
    /** Text color for a hovered, source-transferred enchantment. */
    @Unique
    private static final int ENCHANTING_FROM_SOURCE_TEXT_HIGHLIGHTED_COLOR = 0x000000;
    /** Text color for a disabled, source-transferred enchantment. */
    @Unique
    private static final int ENCHANTING_FROM_SOURCE_TEXT_DISABLED_COLOR = 0xFFFFFF;

    /** Text color for an enabled, target-upgrade enchantment. */
    @Unique
    private static final int ENCHANTING_FROM_TARGET_TEXT_ENABLED_COLOR = 0xFFFF80;
    /** Text color for a hovered, target-upgrade enchantment. */
    @Unique
    private static final int ENCHANTING_FROM_TARGET_TEXT_HIGHLIGHTED_COLOR = 0x000000;
    /** Text color for a disabled, target-upgrade enchantment. */
    @Unique
    private static final int ENCHANTING_FROM_TARGET_TEXT_DISABLED_COLOR = 0xFFFFFF;

    /** Text color for an enchantment that is already at its maximum level. */
    @Unique
    private static final int ENCHANTMENT_MAXED_OUT_TEXT_COLOR = 0xF2F09D;
    /** Text color for an enchantment that is already at its maximum level. */
    @Unique
    private static final int ENCHANTMENT_OVER_ENCHANTED_TEXT_COLOR = 0x4D2299;

    /** Text color for the level requirement number when affordable. */
    @Unique
    private static final int ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR = 0x80FF20;
    /** Text color for the level requirement number when unaffordable. */
    @Unique
    private static final int ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR = 0x408000;
    // endregion

    // region Shadow Fields
    /** Shadowed field for the vanilla cost icon textures (e.g., "1 XP"). */
    @Shadow
    @Final
    private static ResourceLocation[] ENABLED_LEVEL_SPRITES;
    /** Shadowed field for the vanilla disabled cost icon textures. */
    @Shadow
    @Final
    private static ResourceLocation[] DISABLED_LEVEL_SPRITES;

    /** Shadowed method, unused in this mixin but required by the parent class. */
    @Shadow
    public abstract void containerTick();
    // endregion

    // region Constructor
    /**
     * Constructs the screen, matching the superclass constructor.
     *
     * @param handler   The screen handler.
     * @param inventory The player inventory.
     * @param title     The screen title.
     */
    public EnchantmentScreenMixin(EnchantmentMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    /**
     * Modifies the arguments passed to the HandledScreen super() constructor call
     * to obfuscate the screen's title.
     */
    @ModifyArgs(method = "<init>",
                    at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;)V")
    )
    private static void modifySuperConstructorArgs(Args args) {
        // The title is at index 2
        Component originalTitle = args.get(2);
        if (originalTitle != null) {
            // Get the original style and add the 'minecraft:alt' font
            Style galacticStyle = originalTitle.getStyle().withFont(GALACTIC_FONT_ID);

            // Set the new title with the applied style
            args.set(2, originalTitle.copy().setStyle(galacticStyle).withStyle(ChatFormatting.YELLOW));
        }
    }

    /**
     * Injects into {@code init} to update the {@link #imageHeight} to match
     * the new, taller GUI texture.
     */
    @Inject(method = "init()V", at = @At("TAIL"))
    private void changeBackgroundHeight(CallbackInfo ci) {
        this.imageHeight = SCREEN_ORIGINAL_HEIGHT + SCREEN_HEIGHT_ADJUSTMENT;
    } // endregion

    // region Draw Background
    @Inject(method = "renderBg", at = @At("HEAD"), cancellable = true)
    private void drawCustomBackground(GuiGraphics context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        // 1. Background Calculation & Draw
        int alignX = this.leftPos;
        int alignY = this.topPos;
        context.blit(BACKGROUND_TEXTURE, alignX, alignY, 0, 0, this.imageWidth, this.imageHeight);

        // 1b. Get Config Flag
        boolean usePlain = Config.BINARY_ACCESSIBILITY_USE_PLAIN_BACKGROUND.get();

        // 2. Seed & Lapis
        EnchantmentNames.getInstance().initSeed((long) ((EnchantmentMenu) this.menu).getEnchantmentSeed());
        // Lapis count logic: accessing the slot directly as per mixin changes
        int lapisCount = ((EnchantmentMenu) this.menu).getSlot(1).getItem().getCount();

        // 3. Get Data from Accessor
        EnchantmentMenuAccessor accessor = (EnchantmentMenuAccessor) this.menu;
        int[] enchantmentSources = accessor.enchanting_overhauled$getEnchantmentSourceArray();
        // Get texture indices from handler
        int[] targetTextureIndices = accessor.enchanting_overhauled$getTargetTextureIndices();
        int[] sourceTextureIndices = accessor.enchanting_overhauled$getSourceTextureIndices();
        int[] tableTextureIndices = accessor.enchanting_overhauled$getTableTextureIndices();

        // 4. Loop and Dispatch Slots 0-2
        for (int buttonIndex = 0; buttonIndex < REROLL_BUTTON_INDEX; ++buttonIndex) {
            int buttonX = alignX + ENCHANTING_BUTTON_X_OFFSET;
            int buttonY = alignY + ENCHANTING_BUTTON_Y_OFFSET + ENCHANTING_BUTTON_HEIGHT * buttonIndex;
            int enchantingPower = ((EnchantmentMenu) this.menu).costs[buttonIndex];
            int source = enchantmentSources[buttonIndex];

            int id = ((EnchantmentMenu) this.menu).enchantClue[buttonIndex];

            Holder<Enchantment> enchantment = null;
            if (id >= 0 && this.minecraft.level != null) {
                IdMap<Holder<Enchantment>> idMap =
                        this.minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
                enchantment = idMap.byId(id);
            }

            int level = ((EnchantmentMenu) this.menu).levelClue[buttonIndex];

            if (enchantingPower <= 0 || enchantment == null) {
                // Case 1: Empty Slot
                this.enchanting_Overhauled$drawEmptySlot(
                        context,
                        buttonX,
                        buttonY,
                        tableTextureIndices[buttonIndex],
                        usePlain
                );
            } else if (source == EnchantmentSource.TARGET.getId()) {
                // Case 2: Upgrade Slot
                this.enchanting_Overhauled$drawUpgradeSlot(
                        context,
                        accessor,
                        buttonIndex,
                        buttonX,
                        buttonY,
                        mouseX,
                        mouseY,
                        lapisCount,
                        enchantingPower,
                        enchantment,
                        level,
                        targetTextureIndices[buttonIndex],
                        usePlain
                );
            } else if (source == EnchantmentSource.SOURCE.getId()) {
                // Case 3: Transfer Slot
                this.enchanting_Overhauled$drawTransferSlot(
                        context,
                        accessor,
                        buttonIndex,
                        buttonX,
                        buttonY,
                        mouseX,
                        mouseY,
                        lapisCount,
                        enchantingPower,
                        enchantment,
                        level,
                        sourceTextureIndices[buttonIndex],
                        usePlain
                );
            } else if (source == EnchantmentSource.TABLE.getId()) {
                // Case 4: Apply Slot
                this.enchanting_Overhauled$drawApplySlot(
                        context,
                        accessor,
                        buttonIndex,
                        buttonX,
                        buttonY,
                        mouseX,
                        mouseY,
                        lapisCount,
                        enchantingPower,
                        enchantment,
                        level,
                        tableTextureIndices[buttonIndex],
                        usePlain
                );
            } else {
                // Fallback: Empty Slot
                this.enchanting_Overhauled$drawEmptySlot(
                        context,
                        buttonX,
                        buttonY,
                        tableTextureIndices[buttonIndex],
                        usePlain
                );
            }
        }

        // 5. Draw Reroll Button
        this.enchanting_Overhauled$drawRerollButton(
                context,
                accessor,
                alignX,
                alignY,
                mouseX,
                mouseY,
                lapisCount,
                enchantmentSources
        );

        // 6. Draw Experience Bar
        this.enchanting_Overhauled$drawExperienceBar(
                context,
                alignX,
                alignY
        );

        // 7. Cancel Vanilla Method
        ci.cancel();
    }

    /**
     * Draws a standard disabled enchantment slot.
     * Called when {@code enchantingPower <= 0}.
     */
    @Unique
    private void enchanting_Overhauled$drawEmptySlot(
            GuiGraphics context,
            int buttonX,
            int buttonY,
            int texIndex,
            boolean usePlain
    ) {
        RenderSystem.enableBlend();
        ResourceLocation disabledTex = usePlain ? PLAIN_TABLE_DISABLED_TEXTURE
                : ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                "textures/gui/container/enchanting_table/button/disabled/table_" + texIndex + ".png");
        context.blit(disabledTex, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT,
                ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);
        RenderSystem.disableBlend();
    }

    /**
     * Draws an "Upgrade" slot (source == TARGET).
     * This slot can be in an enabled, highlighted, disabled, or maxed-out state.
     */
    @Unique
    private void enchanting_Overhauled$drawUpgradeSlot(
            GuiGraphics context,
            EnchantmentMenuAccessor accessor,
            int buttonIndex,
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
            // This slot is for an enchantment that is already max level
            this.enchanting_Overhauled$drawMaxedSlot(
                    context,
                    enchantment,
                    enchantmentLevel,
                    buttonX,
                    buttonY
            );
        } else {
            // This is a standard, non-maxed upgrade slot
            int cost = accessor.enchanting_overhauled$calculateEnchantmentCost(enchantment.value());
            // Affordability check for upgrades (uses buttonIndex + 1 for lapis)
            // Using 'minecraft.player' to access client player
            boolean affordable = (lapisCount >= cost && this.minecraft.player.experienceLevel >= enchantingPower
                    && this.minecraft.player.experienceLevel >= cost) || this.minecraft.player.isCreative();

            // Generate textures dynamically
            ResourceLocation enabledTex;
            ResourceLocation highlightedTex;
            ResourceLocation disabledTex;

            if (usePlain) {
                enabledTex = PLAIN_TARGET_ENABLED_TEXTURE;
                highlightedTex = PLAIN_TARGET_HIGHLIGHTED_TEXTURE;
                disabledTex = PLAIN_TARGET_DISABLED_TEXTURE;
            } else {
                enabledTex = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                        "textures/gui/container/enchanting_table/button/enabled/target_" + texIndex + ".png");
                highlightedTex = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                        "textures/gui/container/enchanting_table/button/highlighted/target_" + texIndex + ".png");
                disabledTex = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                        "textures/gui/container/enchanting_table/button/disabled/target_" + texIndex + ".png");
            }

            this.enchanting_Overhauled$drawEnchantmentSlot(
                    context, buttonX, buttonY, mouseX, mouseY,
                    enchantingPower, cost, affordable, false,
                    enabledTex,
                    highlightedTex,
                    disabledTex,
                    Enchantment.getFullname(enchantment, enchantmentLevel),
                    ENCHANTING_FROM_TARGET_TEXT_ENABLED_COLOR,
                    ENCHANTING_FROM_TARGET_TEXT_HIGHLIGHTED_COLOR,
                    ENCHANTING_FROM_TARGET_TEXT_DISABLED_COLOR);
        }
    }

    /**
     * Draws a "Transfer" slot (source == SOURCE).
     * This slot can be in an enabled, highlighted, or disabled state.
     */
    @Unique
    private void enchanting_Overhauled$drawTransferSlot(
            GuiGraphics context,
            EnchantmentMenuAccessor accessor,
            int buttonIndex,
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
        int cost = accessor.enchanting_overhauled$calculateEnchantmentCost(enchantment.value());
        // Affordability check for transfer/apply (uses enchantingPower for lapis)
        boolean affordable = (lapisCount >= cost && this.minecraft.player.experienceLevel >= enchantingPower
                && this.minecraft.player.experienceLevel >= cost) || this.minecraft.player.isCreative();

        // Generate textures dynamically
        ResourceLocation enabledTex;
        ResourceLocation highlightedTex;
        ResourceLocation disabledTex;

        if (usePlain) {
            enabledTex = PLAIN_SOURCE_ENABLED_TEXTURE;
            highlightedTex = PLAIN_SOURCE_HIGHLIGHTED_TEXTURE;
            disabledTex = PLAIN_SOURCE_DISABLED_TEXTURE;
        } else {
            enabledTex = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/enabled/source_" + texIndex + ".png");
            highlightedTex = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/highlighted/source_" + texIndex + ".png");
            disabledTex = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/disabled/source_" + texIndex + ".png");
        }

        this.enchanting_Overhauled$drawEnchantmentSlot(
                context,
                buttonX,
                buttonY,
                mouseX,
                mouseY,
                enchantingPower, cost, affordable, false,
                enabledTex,
                highlightedTex,
                disabledTex,
                Enchantment.getFullname(enchantment, enchantmentLevel),
                ENCHANTING_FROM_SOURCE_TEXT_ENABLED_COLOR,
                ENCHANTING_FROM_SOURCE_TEXT_HIGHLIGHTED_COLOR,
                ENCHANTING_FROM_SOURCE_TEXT_DISABLED_COLOR
        );
    }

    /**
     * Draws an "Apply" slot (source == TABLE).
     * This slot can be in an enabled, highlighted, or disabled state.
     */
    @Unique
    private void enchanting_Overhauled$drawApplySlot(
            GuiGraphics context,
            EnchantmentMenuAccessor accessor,
            int buttonIndex,
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
        int cost = accessor.enchanting_overhauled$calculateEnchantmentCost(enchantment.value());
        // Affordability check for transfer/apply (uses enchantingPower for lapis)
        boolean affordable = (lapisCount >= cost && this.minecraft.player.experienceLevel >= enchantingPower
                && this.minecraft.player.experienceLevel >= cost) || this.minecraft.player.isCreative();

        // Generate textures dynamically
        ResourceLocation enabledTex;
        ResourceLocation highlightedTex;
        ResourceLocation disabledTex;

        if (usePlain) {
            enabledTex = PLAIN_TABLE_ENABLED_TEXTURE;
            highlightedTex = PLAIN_TABLE_HIGHLIGHTED_TEXTURE;
            disabledTex = PLAIN_TABLE_DISABLED_TEXTURE;
        } else {
            enabledTex = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/enabled/table_" + texIndex + ".png");
            highlightedTex = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/highlighted/table_" + texIndex + ".png");
            disabledTex = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                    "textures/gui/container/enchanting_table/button/disabled/table_" + texIndex + ".png");
        }

        // Get the original enchantment name
        Component originalName = Enchantment.getFullname(enchantment, enchantmentLevel);
        // Get the original style and add the 'minecraft:alt' font
        Style galacticStyle = originalName.getStyle().withFont(GALACTIC_FONT_ID);
        // Create the new text component with the applied style
        Component galacticName = originalName.copy().setStyle(galacticStyle);

        Component name = Config.BINARY_ACCESSIBILITY_OBFUSCATE_NEW_ENCHANTMENTS.get()
                ? galacticName
                : originalName;

        this.enchanting_Overhauled$drawEnchantmentSlot(
                context, buttonX, buttonY, mouseX, mouseY,
                enchantingPower, cost, affordable, false,
                enabledTex,
                highlightedTex,
                disabledTex,
                name,
                ENCHANTING_FROM_TABLE_TEXT_ENABLED_COLOR,
                ENCHANTING_FROM_TABLE_TEXT_HIGHLIGHTED_COLOR,
                ENCHANTING_FROM_TABLE_TEXT_DISABLED_COLOR);
    }

    /**
     * Draws a generic enchantment slot that is not maxed out.
     * This is the base logic for upgrade, transfer, and apply slots, handling
     * texture selection, text rendering, and cost icon display based on state.
     */
    @Unique
    private void enchanting_Overhauled$drawEnchantmentSlot(
            GuiGraphics context,
            int buttonX,
            int buttonY,
            int mouseX,
            int mouseY,
            int enchantingPower,
            int cost,
            boolean affordable,
            boolean isMaxed,
            ResourceLocation enabledTex,
            ResourceLocation highlightedTex,
            ResourceLocation disabledTex,
            Component enchantmentName,
            int enabledColor,
            int highlightedColor,
            int disabledColor
    ) {
        int costIndex = Math.max(0, cost - 1);
        int textX = buttonX + ENCHANTING_TEXT_X_OFFSET;
        int textY = buttonY + ENCHANTING_TEXT_Y_OFFSET;

        String string = "" + enchantingPower;
        int enchantingPowerX = ENCHANTING_POWER_X_OFFSET - this.font.width(string);
        int colorCode;

        if (!affordable) {
            RenderSystem.enableBlend();
            context.blit(disabledTex, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT,
                    ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);
            context.blitSprite(DISABLED_LEVEL_SPRITES[costIndex], buttonX + ENCHANTING_COST_X_OFFSET,
                    buttonY + ENCHANTING_COST_Y_OFFSET, ENCHANTING_COST_WIDTH, ENCHANTING_COST_HEIGHT);
            RenderSystem.disableBlend();

            // Draw the enchantment name
            context.drawWordWrap(this.font, enchantmentName, textX, textY, ENCHANTING_TEXT_MAX_WIDTH,
                    disabledColor);

            colorCode = ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR; // disabled num color
        } else {
            int mouseDiffX = mouseX - buttonX;
            int mouseDiffY = mouseY - buttonY;
            boolean hovering = mouseDiffX >= 0 && mouseDiffY >= 0 && mouseDiffX < ENCHANTING_BUTTON_WIDTH
                    && mouseDiffY < ENCHANTING_BUTTON_HEIGHT;
            RenderSystem.enableBlend();

            int nameColor; // Color for the enchantment name

            if (hovering) {
                context.blit(highlightedTex, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH,
                        ENCHANTING_BUTTON_HEIGHT, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);
                nameColor = highlightedColor;
            } else {
                context.blit(enabledTex, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH,
                        ENCHANTING_BUTTON_HEIGHT, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);
                nameColor = enabledColor;
            }

            context.blitSprite(ENABLED_LEVEL_SPRITES[costIndex], buttonX + ENCHANTING_COST_X_OFFSET,
                    buttonY + ENCHANTING_COST_Y_OFFSET, ENCHANTING_COST_WIDTH, ENCHANTING_COST_HEIGHT);
            RenderSystem.disableBlend();

            // Draw the enchantment name
            context.drawWordWrap(this.font, enchantmentName, textX, textY, ENCHANTING_TEXT_MAX_WIDTH,
                    nameColor);

            colorCode = ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR; // default num color
        }

        // Hide Level Requirement if Maxed
        int outlineColor = 0;
        if (!isMaxed) {
            // Draw outline
            context.drawString(this.font, string,
                    textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string) + 1,
                    buttonY + ENCHANTING_POWER_Y_OFFSET, outlineColor, false);
            context.drawString(this.font, string,
                    textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string) - 1,
                    buttonY + ENCHANTING_POWER_Y_OFFSET, outlineColor, false);
            context.drawString(this.font, string,
                    textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string),
                    buttonY + ENCHANTING_POWER_Y_OFFSET + 1, outlineColor, false);
            context.drawString(this.font, string,
                    textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string),
                    buttonY + ENCHANTING_POWER_Y_OFFSET - 1, outlineColor, false);

            // Draw level requirement
            context.drawString(
                    this.font,
                    string,
                    textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string),
                    buttonY + ENCHANTING_POWER_Y_OFFSET,
                    colorCode,
                    false);
            // Draw shadow manually or just use drawString with shadow param if available
            // Mojang usually handles shadow with a boolean or different method
            // Using standard drawString with no shadow as per above, then draw with shadow if needed.
            // Vanilla method: drawString(font, text, x, y, color, dropShadow)
            context.drawString(this.font, string,
                    textX + ENCHANTING_POWER_X_OFFSET - this.font.width(string),
                    buttonY + ENCHANTING_POWER_Y_OFFSET, colorCode, true);
        }
    }

    /**
     * Draws a slot that is "Maxed Out" (source == TARGET and level >= maxLevel)
     * or "Over-Enchanted" (level > maxLevel).
     * This slot is non-interactive and displays the enchantment's name
     * centered within the button.
     */
    @Unique
    private void enchanting_Overhauled$drawMaxedSlot(
            GuiGraphics context,
            Holder<Enchantment> enchantment,
            int enchantmentLevel,
            int buttonX,
            int buttonY
    ) {
        // 1. Get the fully formatted name
        Component enchantmentName = Enchantment.getFullname(enchantment, enchantmentLevel);

        // 2. Calculate coordinates
        int textY = buttonY + ENCHANTING_TEXT_Y_OFFSET;
        // int textWidth = this.font.width(enchantmentName); // Unused variable
        int textX = buttonX + 8;

        // 3. Draw the background texture
        ResourceLocation backgroundTexture = (enchantmentLevel > enchantment.value().getMaxLevel())
                ? ENCHANTMENT_SLOT_OVER_ENCHANTED_TEXTURE
                : ENCHANTMENT_SLOT_MAXED_OUT_TEXTURE;
        context.blit(
                backgroundTexture,
                buttonX,
                buttonY,
                0,
                0,
                ENCHANTING_BUTTON_WIDTH,
                ENCHANTING_BUTTON_HEIGHT,
                ENCHANTING_BUTTON_WIDTH,
                ENCHANTING_BUTTON_HEIGHT);

        // 4. Draw the text, centered, with no shadow.
        int color = (enchantmentLevel > enchantment.value().getMaxLevel())
                ? ENCHANTMENT_OVER_ENCHANTED_TEXT_COLOR
                : ENCHANTMENT_MAXED_OUT_TEXT_COLOR;
        context.drawString(
                this.font,
                enchantmentName,
                textX,
                textY,
                color,
                false // no shadow
        );
    }

    /**
     * Draws the "Turn Page" reroll button and its associated cost icon.
     * Selects the correct texture (normal, highlighted, disabled) based on
     * mouse position and whether the reroll action is valid and affordable.
     */
    @Unique
    private void enchanting_Overhauled$drawRerollButton(
            GuiGraphics context,
            EnchantmentMenuAccessor accessor,
            int alignX,
            int alignY,
            int mouseX,
            int mouseY,
            int lapisCount,
            int[] enchantmentSources
    ) {
        // Get Reroll Button Logic
        ItemStack target = accessor.enchanting_overhauled$getEnchantmentTarget();
        boolean targetIsEmpty = target.isEmpty();
        boolean targetIsEnchantable = !targetIsEmpty && (target.is(Items.BOOK) || target.isEnchantable());
        boolean targetIsSourceEnchantable = Arrays.stream(enchantmentSources)
                                                  .anyMatch(element -> element == EnchantmentSource.SOURCE.getId());

        ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
        int occupiedSlots = EnchantmentLib.getEnchantments(curseFreeTarget).size();

        int rerollCost = occupiedSlots + 1;
        int costIndex = Math.max(0, rerollCost - 1); // Clamp index
        boolean hasTableSource = Arrays.stream(enchantmentSources)
                                       .anyMatch(source -> source == EnchantmentSource.TABLE.getId());
        boolean canReroll = occupiedSlots < 3 && !targetIsSourceEnchantable && targetIsEnchantable && hasTableSource;

        int x = alignX + REROLL_BUTTON_X_OFFSET;
        int y = alignY + REROLL_BUTTON_Y_OFFSET;

        RenderSystem.enableBlend();

        boolean cannotAfford = (lapisCount < rerollCost || this.minecraft.player.experienceLevel < rerollCost)
                && !this.minecraft.player.isCreative();

        boolean rerollEnabled = !targetIsEmpty && targetIsEnchantable && canReroll && !cannotAfford;

        if (rerollEnabled) {
            // Draw enabled/highlighted
            int mouseXdiff = mouseX - x;
            int mouseYdiff = mouseY - y;

            if (mouseXdiff >= 0 && mouseYdiff >= 0 && mouseXdiff < REROLL_BUTTON_WIDTH
                    && mouseYdiff < REROLL_BUTTON_HEIGHT) {
                context.blit(REROLL_HIGHLIGHTED_TEXTURE, x, y, 0, 0, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT,
                        REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT);
            } else {
                context.blit(REROLL_TEXTURE, x, y, 0, 0, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT,
                        REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT);
            }
            // costIndex is safe because rerollEnabled -> canReroll -> occupiedSlots < 3
            if (costIndex < ENABLED_LEVEL_SPRITES.length) {
                context.blitSprite(ENABLED_LEVEL_SPRITES[costIndex], x + REROLL_COST_X_OFFSET, y + REROLL_COST_Y_OFFSET,
                        REROLL_COST_WIDTH, REROLL_COST_HEIGHT);
            }
        } else {
            // Draw disabled
            context.blit(REROLL_DISABLED_TEXTURE, x, y, 0, 0, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT,
                    REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT);

            // Show disabled cost only if the *only* reason it's disabled is cost
            boolean showDisabledCost = !targetIsEmpty && targetIsEnchantable && canReroll;
            if (showDisabledCost && costIndex < DISABLED_LEVEL_SPRITES.length) {
                // costIndex is safe because showDisabledCost -> canReroll -> occupiedSlots < 3
                context.blitSprite(DISABLED_LEVEL_SPRITES[costIndex], x + REROLL_COST_X_OFFSET,
                        y + REROLL_COST_Y_OFFSET, REROLL_COST_WIDTH, REROLL_COST_HEIGHT);
            }
        }
        RenderSystem.disableBlend();
    }
    // endregion

    // region Draw Experience Bar
    /**
     * Helper method to draw the player's experience bar and level number.
     * This replaces the logic previously handled by {@code drawExperienceBar}.
     */
    @Unique
    private void enchanting_Overhauled$drawExperienceBar(GuiGraphics context, int alignX, int alignY) {
        // 1. Define Bar Geometry
        int barX = alignX + EXPERIENCE_BAR_X_OFFSET;
        int barY = alignY + EXPERIENCE_BAR_Y_OFFSET;
        int barWidth = EXPERIENCE_BAR_WIDTH;
        int barHeight = EXPERIENCE_BAR_HEIGHT;

        // 2. Draw Inactive Bar
        // We draw the full inactive bar first as a background.
        context.blit(EXPERIENCE_BAR_INACTIVE, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        // 3. Draw Active Bar
        float progress = this.minecraft.player.experienceProgress;
        int activeWidth = (int) (progress * (float) barWidth); // Calculate fill width

        // Draw the active bar, cropped to the calculated width
        if (activeWidth > 0) {
            context.blit(EXPERIENCE_BAR_ACTIVE, barX, barY, 0, 0, activeWidth, barHeight, barWidth, barHeight);
        }

        // 4. Draw Level Number
        int playerLevel = this.minecraft.player.experienceLevel;
        if (playerLevel > 0) {
            String levelString = "" + playerLevel;
            // int textWidth = this.font.width(levelString); // Unused var

            // Center the text horizontally over the bar
            int textX = barX + (barWidth / 2) - (this.font.width(levelString) / 2);
            int textY = alignY + EXPERIENCE_BAR_Y_OFFSET - 6;

            // Draw the black outline
            int outlineColor = 0; // Black
            context.drawString(this.font, levelString, textX + 1, textY, outlineColor, false); // Right
            context.drawString(this.font, levelString, textX - 1, textY, outlineColor, false); // Left
            context.drawString(this.font, levelString, textX, textY + 1, outlineColor, false); // Down
            context.drawString(this.font, levelString, textX, textY - 1, outlineColor, false); // Up

            // Draw the main text
            context.drawString(
                    this.font,
                    levelString,
                    textX,
                    textY,
                    ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR, // 8453920
                    false // No shadow
            );
        }
    } // endregion

    // region Interaction handler
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Use the new imageHeight (186) to calculate 'alignY'
        int alignX = this.leftPos;
        int alignY = this.topPos;

        // Check enchantment buttons (0-2)
        for (int buttonIndex = 0; buttonIndex < REROLL_BUTTON_INDEX; ++buttonIndex) {
            double mouseDiffX = mouseX - (double) (alignX + ENCHANTING_BUTTON_X_OFFSET);
            double mouseDiffY = mouseY
                    - (double) (alignY + ENCHANTING_BUTTON_Y_OFFSET + ENCHANTING_BUTTON_HEIGHT * buttonIndex);

            if (mouseDiffX >= 0.0F && mouseDiffY >= 0.0F && mouseDiffX < ENCHANTING_BUTTON_WIDTH
                    && mouseDiffY < ENCHANTING_BUTTON_HEIGHT
                    && ((EnchantmentMenu) this.menu).clickMenuButton(this.minecraft.player, buttonIndex)) {
                this.minecraft.gameMode.handleInventoryButtonClick(((EnchantmentMenu) this.menu).containerId,
                        buttonIndex);
                return true;
            }
        }

        // Check reroll button (3)
        double mouseXdiff = mouseX - (double) (alignX + REROLL_BUTTON_X_OFFSET);
        double mouseYdiff = mouseY - (double) (alignY + REROLL_BUTTON_Y_OFFSET);

        if (mouseXdiff >= 0.0F && mouseYdiff >= 0.0F && mouseXdiff < (double) REROLL_BUTTON_WIDTH
                && mouseYdiff < (double) REROLL_BUTTON_HEIGHT
                && this.menu.clickMenuButton(this.minecraft.player, REROLL_BUTTON_INDEX)) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, REROLL_BUTTON_INDEX);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    } // endregion

    // region Render
    /**
     * Replaces the vanilla {@code render} method to implement custom tooltip logic.
     * <p>
     * This method now acts as a dispatcher, canceling the vanilla render,
     * calling the super.render() (which triggers our custom background draw),
     * and then delegating tooltip logic to specific helper methods.
     *
     * @param context The draw context.
     * @param mouseX  Mouse X position.
     * @param mouseY  Mouse Y position.
     * @param delta   Frame delta time.
     * @param ci      Callback info to cancel the original method.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void overrideRender(
            GuiGraphics context,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        ci.cancel(); // Cancel the vanilla render method entirely

        // 1. This is the body of super.render()
        // It will call our injected drawCustomBackground()
        super.render(context, mouseX, mouseY, delta);

        // 2. This is the vanilla call for item tooltips
        this.renderTooltip(context, mouseX, mouseY);

        // 3. Get common data for helpers
        boolean isCreative = this.minecraft.player.isCreative();
        // Direct slot access as 'getLapisCount' is replaced
        int lapisCount = ((EnchantmentMenu) this.menu).getSlot(1).getItem().getCount();
        EnchantmentMenuAccessor accessor = (EnchantmentMenuAccessor) this.menu;

        // 4. Try drawing enchantment slot tooltips
        // This helper returns true if it drew a tooltip, so we can stop.
        boolean tooltipDrawn = this.enchanting_Overhauled$drawEnchantmentSlotTooltips(
                context,
                mouseX,
                mouseY,
                accessor,
                lapisCount,
                isCreative
        );

        // 5. If no enchantment tooltip was drawn, try drawing the reroll tooltip
        if (!tooltipDrawn) {
            this.enchanting_Overhauled$drawRerollButtonTooltip(
                    context,
                    mouseX,
                    mouseY,
                    accessor,
                    lapisCount,
                    isCreative
            );
        }
    }

    /**
     * Helper to safely get the description translation key.
     * Returns null if the holder is not bound or has no key.
     */
    @Unique
    private String enchanting_Overhauled$getSafeDescriptionKey(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey()
                .map(key -> Util.makeDescriptionId("enchantment", key.location()) + ".desc")
                .orElse(null);
    }

    /**
     * Draws the tooltips for the three enchantment slots (0-2) by dispatching
     * to the correct helper method based on the enchantment's source.
     */
    @Unique
    private boolean enchanting_Overhauled$drawEnchantmentSlotTooltips(
            GuiGraphics context,
            int mouseX,
            int mouseY,
            EnchantmentMenuAccessor accessor,
            int lapisCount,
            boolean isCreative
    ) {
        int[] enchantmentSources = accessor.enchanting_overhauled$getEnchantmentSourceArray();

        for (int buttonIndex = 0; buttonIndex < REROLL_BUTTON_INDEX; ++buttonIndex) {
            int powerRequirement = ((EnchantmentMenu) this.menu).costs[buttonIndex];
            int id = ((EnchantmentMenu) this.menu).enchantClue[buttonIndex];

            // Updated Lookup
            Holder<Enchantment> enchantment = null;
            if (id >= 0 && this.minecraft.level != null) {
                IdMap<Holder<Enchantment>> idMap =
                        this.minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
                enchantment = idMap.byId(id);
            }

            int enchantmentLevel = ((EnchantmentMenu) this.menu).levelClue[buttonIndex];
            int source = enchantmentSources[buttonIndex];
            int buttonY = ENCHANTING_BUTTON_Y_OFFSET + (ENCHANTING_BUTTON_HEIGHT * buttonIndex);

            if (this.isHovering(ENCHANTING_BUTTON_X_OFFSET, buttonY, ENCHANTING_BUTTON_WIDTH,
                    ENCHANTING_BUTTON_HEIGHT, (double) mouseX, (double) mouseY) && powerRequirement > 0
                    && enchantmentLevel >= 0 && enchantment != null) { // Check holder != null

                boolean isMaxed = (source == EnchantmentSource.TARGET.getId()
                                          && enchantmentLevel >= enchantment.value().getMaxLevel());

                if (isMaxed) {
                    this.enchanting_Overhauled$drawMaxedTooltip(
                            context,
                            mouseX,
                            mouseY,
                            enchantment,
                            enchantmentLevel
                    );
                    return true;
                } else if (source == EnchantmentSource.TARGET.getId()) {
                    this.enchanting_Overhauled$drawUpgradeTooltip(
                            context,
                            mouseX,
                            mouseY,
                            accessor,
                            lapisCount,
                            isCreative,
                            enchantment,
                            enchantmentLevel,
                            powerRequirement
                    );
                    return true;
                } else if (source == EnchantmentSource.SOURCE.getId()) {
                    this.enchanting_Overhauled$drawTransferTooltip(
                            context,
                            mouseX,
                            mouseY,
                            accessor,
                            lapisCount,
                            isCreative,
                            enchantment,
                            enchantmentLevel,
                            powerRequirement
                    );
                    return true;
                } else if (source == EnchantmentSource.TABLE.getId()) {
                    this.enchanting_Overhauled$drawApplyTooltip(
                            context,
                            mouseX,
                            mouseY,
                            accessor,
                            lapisCount,
                            isCreative,
                            enchantment,
                            enchantmentLevel,
                            powerRequirement
                    );
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Draws the tooltip for a "Maxed Out" or "Over-Leveled" (TARGET) slot.
     * This version has no title and no cost information.
     */
    @Unique
    private void enchanting_Overhauled$drawMaxedTooltip(
            GuiGraphics context,
            int mouseX,
            int mouseY,
            Holder<Enchantment> enchantment,
            int enchantmentLevel
    ) {
        java.util.List<FormattedCharSequence> list = new ArrayList<>();

        // Add the enchantment name
        list.add(Enchantment.getFullname(enchantment, enchantmentLevel).copy().getVisualOrderText());

        // Add enchantment description
        String descKey = enchanting_Overhauled$getSafeDescriptionKey(enchantment);
        if (descKey != null && Config.BINARY_ACCESSIBILITY_SHOW_ENCHANTMENT_DESCRIPTIONS.get()) {
            Component description = Component.translatable(descKey).withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR.get());;
            list.addAll(EnchantmentLib.wrapDescription(description));
        }

        context.renderTooltip(this.font, list, mouseX, mouseY);
    }

    /**
     * Draws the tooltip for a standard, non-maxed "Upgrade" (TARGET) slot.
     */
    @Unique
    private void enchanting_Overhauled$drawUpgradeTooltip(
            GuiGraphics context,
            int mouseX,
            int mouseY,
            EnchantmentMenuAccessor accessor,
            int lapisCount,
            boolean isCreative,
            Holder<Enchantment> enchantment,
            int enchantmentLevel,
            int powerRequirement
    ) {
        int cost = accessor.enchanting_overhauled$calculateEnchantmentCost(enchantment.value());
        java.util.List<FormattedCharSequence> list = new ArrayList<>();

        // Add "Upgrade:" title
        list.add(Component.translatable("gui.enchanting_overhauled.upgrade").withStyle(ChatFormatting.WHITE).getVisualOrderText());

        // Add the enchantment name
        list.add(Enchantment.getFullname(enchantment, enchantmentLevel).copy().getVisualOrderText());

        // Add enchantment description (indented via lib method)
        String descKey = enchanting_Overhauled$getSafeDescriptionKey(enchantment);
        if (descKey != null && Config.BINARY_ACCESSIBILITY_SHOW_ENCHANTMENT_DESCRIPTIONS.get()) {
            Component description = Component.translatable(descKey).withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR.get());;
            list.addAll(EnchantmentLib.wrapDescription(description));
        }

        // Add cost information
        this.enchanting_Overhauled$drawTooltipCost(list, isCreative, powerRequirement, cost, lapisCount);

        context.renderTooltip(this.font, list, mouseX, mouseY);
    }

    /**
     * Draws the tooltip for a "Transfer" (SOURCE) slot.
     */
    @Unique
    private void enchanting_Overhauled$drawTransferTooltip(
            GuiGraphics context,
            int mouseX,
            int mouseY,
            EnchantmentMenuAccessor accessor,
            int lapisCount,
            boolean isCreative,
            Holder<Enchantment> enchantment,
            int enchantmentLevel,
            int powerRequirement
    ) {
        int cost = accessor.enchanting_overhauled$calculateEnchantmentCost(enchantment.value());
        java.util.List<FormattedCharSequence> list = new ArrayList<>();

        // Add "Transfer:" title
        list.add(Component.translatable("gui.enchanting_overhauled.transfer")
                          .withStyle(ChatFormatting.WHITE)
                          .getVisualOrderText()
        );

        // Add the enchantment name
        list.add(Enchantment.getFullname(enchantment, enchantmentLevel).copy().getVisualOrderText());

        // Add enchantment description (indented via lib method)
        String descKey = enchanting_Overhauled$getSafeDescriptionKey(enchantment);
        if (descKey != null && Config.BINARY_ACCESSIBILITY_SHOW_ENCHANTMENT_DESCRIPTIONS.get()) {
            Component description = Component.translatable(descKey).withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR.get());
            list.addAll(EnchantmentLib.wrapDescription(description));
        }

        // Add cost information
        this.enchanting_Overhauled$drawTooltipCost(list, isCreative, powerRequirement, cost, lapisCount);

        context.renderTooltip(this.font, list, mouseX, mouseY);
    }

    /**
     * Draws the tooltip for an "Apply" (TABLE) slot.
     * Adds an "Apply:" title and obfuscates text based on config.
     * Applies the enchantment's theme color to its name.
     */
    @Unique
    private void enchanting_Overhauled$drawApplyTooltip(
            GuiGraphics context,
            int mouseX,
            int mouseY,
            EnchantmentMenuAccessor accessor,
            int lapisCount,
            boolean isCreative,
            Holder<Enchantment> enchantment,
            int enchantmentLevel,
            int powerRequirement
    ) {
        int cost = accessor.enchanting_overhauled$calculateEnchantmentCost(enchantment.value());
        java.util.List<FormattedCharSequence> list = new ArrayList<>();

        // --- Get Theme Color ---
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

        // --- Text Obfuscation Logic ---
        MutableComponent enchantmentName = (MutableComponent) Enchantment.getFullname(enchantment, enchantmentLevel);

        // SAFE KEY LOOKUP
        String descKey = enchanting_Overhauled$getSafeDescriptionKey(enchantment);

        // Use a placeholder if descKey is null to prevent null pointer later, though logic implies we just skip description wrapping
        MutableComponent description = (descKey != null)
                ? Component.translatable(descKey).withStyle(ChatFormatting.GRAY)
                : Component.empty();

        Component title = Component.translatable("gui.enchanting_overhauled.apply").withStyle(ChatFormatting.WHITE);

        if (Config.BINARY_ACCESSIBILITY_OBFUSCATE_NEW_ENCHANTMENTS.get()) {
            Style galacticStyle = Style.EMPTY.withFont(GALACTIC_FONT_ID);
            enchantmentName = enchantmentName.setStyle(enchantmentName.getStyle().withFont(GALACTIC_FONT_ID))
                    .withStyle(style -> style.withColor(immutableColor));

            if (descKey != null) {
                description = description.setStyle(description.getStyle().withFont(GALACTIC_FONT_ID))
                        .withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR.get());
            }
        }

        list.add(title.getVisualOrderText());
        list.add(enchantmentName.getVisualOrderText());

        if (descKey != null && Config.BINARY_ACCESSIBILITY_SHOW_ENCHANTMENT_DESCRIPTIONS.get()) {
            list.addAll(EnchantmentLib.wrapDescription(description));
        }

        this.enchanting_Overhauled$drawTooltipCost(list, isCreative, powerRequirement, cost, lapisCount);
        context.renderTooltip(this.font, list, mouseX, mouseY);
    }

    /**
     * Appends the standardized cost information (level, lapis) to a tooltip list.
     */
    @Unique
    private void enchanting_Overhauled$drawTooltipCost(
            java.util.List<FormattedCharSequence> list,
            boolean isCreative,
            int powerRequirement,
            int cost,
            int lapisCount
    ) {
        // A 'TABLE' enchantment can't be 'isMaxed', so we only check 'isCreative'
        if (!isCreative) {
            list.add(FormattedCharSequence.EMPTY);
            if (this.minecraft.player.experienceLevel < powerRequirement) {
                // Level requirement
                list.add(Component.translatable("container.enchant.level.requirement",
                                new Object[] { powerRequirement }).withStyle(ChatFormatting.RED).getVisualOrderText());
            } else {
                // Lapis requirement
                MutableComponent mutableText;
                if (cost == 1) {
                    mutableText = Component.translatable("container.enchant.lapis.one");
                } else {
                    mutableText = Component.translatable("container.enchant.lapis.many", new Object[] { cost });
                }
                list.add(mutableText.withStyle(
                                lapisCount >= cost ? ChatFormatting.GRAY : ChatFormatting.RED)
                        .getVisualOrderText());

                // Level cost (for information)
                MutableComponent mutableText2;
                if (cost == 1) {
                    mutableText2 = Component.translatable("container.enchant.level.one");
                } else {
                    mutableText2 = Component.translatable("container.enchant.level.many", new Object[] { cost });
                }
                list.add(mutableText2.withStyle(ChatFormatting.GRAY).getVisualOrderText());
            }
        }
    }

    /**
     * Draws the tooltip for the reroll button (3).
     */
    @Unique
    private void enchanting_Overhauled$drawRerollButtonTooltip(
            GuiGraphics context,
            int mouseX,
            int mouseY,
            EnchantmentMenuAccessor accessor,
            int lapisCount,
            boolean isCreative
    ) {
        if (this.isHovering(REROLL_BUTTON_X_OFFSET, REROLL_BUTTON_Y_OFFSET, REROLL_BUTTON_WIDTH,
                REROLL_BUTTON_HEIGHT, (double) mouseX, (double) mouseY)) {
            // Check reroll validity
            int[] enchantmentSources = accessor.enchanting_overhauled$getEnchantmentSourceArray();
            ItemStack target = accessor.enchanting_overhauled$getEnchantmentTarget();
            boolean targetIsEmpty = target.isEmpty();
            boolean targetIsEnchantable = !targetIsEmpty && (target.is(Items.BOOK) || target.isEnchantable());
            boolean targetIsSourceEnchantable = Arrays.stream(enchantmentSources)
                    .anyMatch(element -> element == EnchantmentSource.SOURCE.getId());
            ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
            int occupiedSlots = EnchantmentLib.getEnchantments(curseFreeTarget).size();
            int rerollCost = occupiedSlots + 1;
            boolean canReroll = occupiedSlots < 3 && !targetIsSourceEnchantable && targetIsEnchantable;

            // Build tooltip
            java.util.List<FormattedCharSequence> list = new ArrayList<>();
            // Assuming you have this translation key for "Turn Page"
            list.add(Component.translatable("gui.enchanting_overhauled.turn_page")
                    .withStyle(ChatFormatting.WHITE).getVisualOrderText());

            // Delegate cost drawing to the generic helper.
            // For a reroll, the 'powerRequirement' and the 'cost' are the same.
            if (!targetIsEmpty && canReroll) {
                this.enchanting_Overhauled$drawTooltipCost(list, isCreative, rerollCost, rerollCost, lapisCount);
            }

            context.renderTooltip(this.font, list, mouseX, mouseY);
        }
    } // endregion
}