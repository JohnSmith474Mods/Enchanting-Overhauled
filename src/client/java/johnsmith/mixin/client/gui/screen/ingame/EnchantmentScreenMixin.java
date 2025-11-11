package johnsmith.mixin.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import johnsmith.EnchantingOverhauled;
import johnsmith.api.enchantment.theme.EnchantmentTheme;
import johnsmith.api.enchantment.theme.accessor.EnchantmentThemeAccessor;
import johnsmith.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.config.Config;
import johnsmith.lib.EnchantmentLib;
import johnsmith.accessor.EnchantmentScreenHandlerAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantingPhrases;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Arrays;

/**
 * Mixin to {@link EnchantmentScreen} to implement the client-side visuals for the
 * enchanting overhaul.
 * <p>
 * This mixin:
 * <ul>
 * <li>Replaces the background texture with a taller version.</li>
 * <li>Completely overrides the background drawing logic ({@link #drawCustomBackground}) to
 * render the new enchantment/upgrade/transfer slots and the reroll button.</li>
 * <li>Implements a custom experience bar ({@link #drawExperienceBar}).</li>
 * <li>Overrides the mouse click handler ({@link #mouseClicked}) to correctly
 * process clicks on the new button layout.</li>
 * <li>Overrides the render method ({@link #overrideRender}) to provide custom
 * tooltips for all buttons and fix enchantment name rendering.</li>
 * </ul>
 */
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends HandledScreen < EnchantmentScreenHandler > {

    // region Font Identifiers
    @Unique
    private static final Identifier GALACTIC_FONT_ID = new Identifier("minecraft", "alt");
    // endregion

    // region Texture Identifiers
    /** The main background texture for the new 176x186 GUI. */
    @Unique
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/background.png");
    /** Texture for the "Turn Page" reroll button in its normal state. */
    @Unique
    private static final Identifier REROLL_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/reroll.png");
    /** Texture for the "Turn Page" reroll button when hovered. */
    @Unique
    private static final Identifier REROLL_HIGHLIGHTED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/reroll.png");
    /** Texture for the "Turn Page" reroll button when disabled. */
    @Unique
    private static final Identifier REROLL_DISABLED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/reroll.png");

    /** Texture for an enchantment slot when the enchantment is at its maximum level. */
    @Unique
    private static final Identifier ENCHANTMENT_SLOT_MAXED_OUT_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/maxed_out.png");

    /** Texture for an enchantment slot when the enchantment is above its maximum level. */
    @Unique
    private static final Identifier ENCHANTMENT_SLOT_OVER_ENCHANTED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/over_enchanted.png");

    /** Texture for the filled (active) part of the player's experience bar. */
    @Unique
    private static final Identifier EXPERIENCE_BAR_ACTIVE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/experience_bar/full.png");
    /** Texture for the empty (inactive) background of the player's experience bar. */

    @Unique
    private static final Identifier EXPERIENCE_BAR_INACTIVE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/experience_bar/empty.png");

    // ADDED FOR ACCESSIBILITY
    @Unique
    private static final Identifier PLAIN_TARGET_ENABLED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/target_plain.png");
    @Unique
    private static final Identifier PLAIN_TARGET_HIGHLIGHTED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/target_plain.png");
    @Unique
    private static final Identifier PLAIN_TARGET_DISABLED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/target_plain.png");

    @Unique
    private static final Identifier PLAIN_SOURCE_ENABLED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/source_plain.png");
    @Unique
    private static final Identifier PLAIN_SOURCE_HIGHLIGHTED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/source_plain.png");
    @Unique
    private static final Identifier PLAIN_SOURCE_DISABLED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/source_plain.png");

    @Unique
    private static final Identifier PLAIN_TABLE_ENABLED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/table_plain.png");
    @Unique
    private static final Identifier PLAIN_TABLE_HIGHLIGHTED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/table_plain.png");
    @Unique
    private static final Identifier PLAIN_TABLE_DISABLED_TEXTURE = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/table_plain.png");
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
    /** The button ID used in {@link EnchantmentScreenHandler#onButtonClick} for the reroll action. */
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
    /** X-coordinate offset from the screen's left edge for the enchantment buttons. */
    @Unique
    private static final int ENCHANTING_BUTTON_X_OFFSET = 60 - 7 - 1;
    /** Y-coordinate offset from the screen's top edge for the first enchantment button. */
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

    // region Source Encoding
    /** Represents an empty or invalid enchantment source. */
    @Unique
    private static final int NONE = -1;
    /** Represents an enchantment originating from the target item (an upgrade). */
    @Unique
    private static final int TARGET = 0;
    /** Represents an enchantment originating from the source item (a transfer). */
    @Unique
    private static final int SOURCE = 1;
    /** Represents an enchantment newly generated by the table. */
    @Unique
    private static final int TABLE = 2;
    // endregion

    // region Shadow Fields
    /** Shadowed field for the vanilla cost icon textures (e.g., "1 XP"). */
    @Shadow
    @Final
    private static Identifier[] LEVEL_TEXTURES;
    /** Shadowed field for the vanilla disabled cost icon textures. */
    @Shadow
    @Final
    private static Identifier[] LEVEL_DISABLED_TEXTURES;
    /** Shadowed field for the vanilla disabled slot texture. */
    @Shadow
    @Final
    private static Identifier TEXTURE;

    /** Shadowed method, unused in this mixin but required by the parent class. */
    @Shadow
    public abstract void doTick();
    // endregion

    // region Constructor
    /**
     * Constructs the screen, matching the superclass constructor.
     * @param handler The screen handler.
     * @param inventory The player inventory.
     * @param title The screen title.
     */
    public EnchantmentScreenMixin(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    /**
     * Modifies the arguments passed to the HandledScreen super() constructor call
     * to obfuscate the screen's title.
     */
    @ModifyArgs(method = "<init>",
                    at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;<init>(Lnet/minecraft/screen/ScreenHandler;Lnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/text/Text;)V"))
    private static void modifySuperConstructorArgs(Args args) {
        // The title is at index 2
        Text originalTitle = args.get(2);
        if (originalTitle != null) {
            // Get the original style and add the 'minecraft:alt' font
            Style galacticStyle = originalTitle.getStyle().withFont(GALACTIC_FONT_ID);

            // Set the new title with the applied style
            args.set(2, originalTitle.copy().setStyle(galacticStyle).formatted(Formatting.YELLOW));
        }
    }

    /**
     * Injects into {@code init} to update the {@link #backgroundHeight} to match the new,
     * taller GUI texture.
     */
    @Inject(method = "init()V", at = @At("TAIL"))
    private void changeBackgroundHeight(CallbackInfo ci) {
        this.backgroundHeight = SCREEN_ORIGINAL_HEIGHT + SCREEN_HEIGHT_ADJUSTMENT;
    } // endregion

    // region Draw Background
    @Inject(method = "drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V",
                at = @At("HEAD"),
       cancellable = true)
    private void drawCustomBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        // 1. Background Calculation & Draw
        int alignX = this.x;
        int alignY = this.y;
        context.drawTexture(BACKGROUND_TEXTURE, alignX, alignY, 0, 0, this.backgroundWidth, this.backgroundHeight);

        // 1b. Get Config Flag
        boolean usePlain = Config.USE_PLAIN_BACKGROUND;

        // 2. Seed & Lapis
        EnchantingPhrases.getInstance().setSeed((long)((EnchantmentScreenHandler) this.handler).getSeed());
        int lapisCount = ((EnchantmentScreenHandler) this.handler).getLapisCount();

        // 3. Get Data from Accessor
        EnchantmentScreenHandlerAccessor accessor = (EnchantmentScreenHandlerAccessor) this.handler;
        int[] enchantmentSources = accessor.getEnchantmentSourceArray();
        // Get texture indices from handler
        int[] targetTextureIndices = accessor.getTargetTextureIndices();
        int[] sourceTextureIndices = accessor.getSourceTextureIndices();
        int[] tableTextureIndices = accessor.getTableTextureIndices();

        // 4. Loop and Dispatch Slots 0-2
        for (int buttonIndex = 0; buttonIndex < REROLL_BUTTON_INDEX; ++buttonIndex) {
            int buttonX = alignX + ENCHANTING_BUTTON_X_OFFSET;
            int buttonY = alignY + ENCHANTING_BUTTON_Y_OFFSET + ENCHANTING_BUTTON_HEIGHT * buttonIndex;
            int enchantingPower = ((EnchantmentScreenHandler) this.handler).enchantmentPower[buttonIndex];
            int source = enchantmentSources[buttonIndex];

            Enchantment enchantment = Enchantment.byRawId(((EnchantmentScreenHandler) this.handler).enchantmentId[buttonIndex]);
            int level = ((EnchantmentScreenHandler) this.handler).enchantmentLevel[buttonIndex];

            if (enchantingPower <= 0 || enchantment == null) {
                // Case 1: Empty Slot
                this.drawEmptySlot(context, buttonX, buttonY, tableTextureIndices[buttonIndex], usePlain);
            } else if (source == TARGET) {
                // Case 2: Upgrade Slot
                this.drawUpgradeSlot(context, accessor, buttonIndex, buttonX, buttonY, mouseX, mouseY, lapisCount, enchantingPower, enchantment, level, targetTextureIndices[buttonIndex], usePlain);
            } else if (source == SOURCE) {
                // Case 3: Transfer Slot
                this.drawTransferSlot(context, accessor, buttonIndex, buttonX, buttonY, mouseX, mouseY, lapisCount, enchantingPower, enchantment, level, sourceTextureIndices[buttonIndex], usePlain);
            } else if (source == TABLE) {
                // Case 4: Apply Slot
                this.drawApplySlot(context, accessor, buttonIndex, buttonX, buttonY, mouseX, mouseY, lapisCount, enchantingPower, enchantment, level, tableTextureIndices[buttonIndex], usePlain);
            } else {
                // Fallback: Empty Slot
                this.drawEmptySlot(context, buttonX, buttonY, tableTextureIndices[buttonIndex], usePlain);
            }
        }

        // 5. Draw Reroll Button
        this.drawRerollButton(context, accessor, alignX, alignY, mouseX, mouseY, lapisCount, enchantmentSources);

        // 6. Draw Experience Bar
        this.drawExperienceBar(context, alignX, alignY);

        // 7. Cancel Vanilla Method
        ci.cancel();
    }

    /**
     * Draws a standard disabled enchantment slot.
     * Called when {@code enchantingPower <= 0}.
     *
     * @param context The draw context.
     * @param buttonX The X-coordinate for the slot.
     * @param buttonY The Y-coordinate for the slot.
     * @param texIndex The texture index (0-9) to use.
     * @param usePlain Whether to use plain textures.
     */
    @Unique
    private void drawEmptySlot(DrawContext context, int buttonX, int buttonY, int texIndex, boolean usePlain) {
        RenderSystem.enableBlend();
        Identifier disabledTex = usePlain ? PLAIN_TABLE_DISABLED_TEXTURE : new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/table_" + texIndex + ".png");
        context.drawTexture(disabledTex, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);
        RenderSystem.disableBlend();
    }

    /**
     * Draws an "Upgrade" slot (source == TARGET).
     * This slot can be in an enabled, highlighted, disabled, or maxed-out state.
     *
     * @param context The draw context.
     * @param accessor The screen handler accessor for getting data.
     * @param buttonIndex The index of the slot (0-2).
     * @param buttonX The X-coordinate for the slot.
     * @param buttonY The Y-coordinate for the slot.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param lapisCount The player's current lapis count.
     * @param enchantingPower The level requirement for this slot.
     * @param enchantment The enchantment being displayed.
     * @param level The level of the enchantment.
     * @param texIndex The texture index (0-9) to use.
     * @param usePlain Whether to use plain textures.
     */
    @Unique
    private void drawUpgradeSlot(DrawContext context, EnchantmentScreenHandlerAccessor accessor, int buttonIndex, int buttonX, int buttonY, int mouseX, int mouseY, int lapisCount, int enchantingPower, Enchantment enchantment, int level, int texIndex, boolean usePlain) {
        boolean isMaxed = (level >= enchantment.getMaxLevel());

        if (isMaxed) {
            // This slot is for an enchantment that is already max level
            this.drawMaxedSlot(
                context,
                enchantment,
                level,
                buttonX,
                buttonY
            );
        } else {
            // This is a standard, non-maxed upgrade slot
            int cost = accessor.calculateEnchantmentCost(enchantment);
            // Affordability check for upgrades (uses buttonIndex + 1 for lapis)
            boolean affordable = (lapisCount >= cost && this.client.player.experienceLevel >= enchantingPower && this.client.player.experienceLevel >= cost) || this.client.player.getAbilities().creativeMode;

            // Generate textures dynamically
            Identifier enabledTex;
            Identifier highlightedTex;
            Identifier disabledTex;

            if (usePlain) {
                enabledTex = PLAIN_TARGET_ENABLED_TEXTURE;
                highlightedTex = PLAIN_TARGET_HIGHLIGHTED_TEXTURE;
                disabledTex = PLAIN_TARGET_DISABLED_TEXTURE;
            } else {
                enabledTex = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/target_" + texIndex + ".png");
                highlightedTex = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/target_" + texIndex + ".png");
                disabledTex = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/target_" + texIndex + ".png");
            }

            this.drawEnchantmentSlot(
                context, buttonX, buttonY, mouseX, mouseY,
                enchantingPower, cost, affordable, false,
                enabledTex,
                highlightedTex,
                disabledTex,
                enchantment.getName(level), // Pass the formatted name
                ENCHANTING_FROM_TARGET_TEXT_ENABLED_COLOR,
                ENCHANTING_FROM_TARGET_TEXT_HIGHLIGHTED_COLOR,
                ENCHANTING_FROM_TARGET_TEXT_DISABLED_COLOR
            );
        }
    }

    /**
     * Draws a "Transfer" slot (source == SOURCE).
     * This slot can be in an enabled, highlighted, or disabled state.
     *
     * @param context The draw context.
     * @param accessor The screen handler accessor for getting data.
     * @param buttonIndex The index of the slot (0-2).
     * @param buttonX The X-coordinate for the slot.
     * @param buttonY The Y-coordinate for the slot.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param lapisCount The player's current lapis count.
     * @param enchantingPower The level requirement for this slot.
     * @param enchantment The enchantment being displayed.
     * @param level The level of the enchantment.
     * @param texIndex The texture index (0-9) to use.
     * @param usePlain Whether to use plain textures.
     */
    @Unique
    private void drawTransferSlot(DrawContext context, EnchantmentScreenHandlerAccessor accessor, int buttonIndex, int buttonX, int buttonY, int mouseX, int mouseY, int lapisCount, int enchantingPower, Enchantment enchantment, int level, int texIndex, boolean usePlain) {
        int cost = accessor.calculateEnchantmentCost(enchantment);
        // Affordability check for transfer/apply (uses enchantingPower for lapis)
        boolean affordable = (lapisCount >= cost && this.client.player.experienceLevel >= enchantingPower && this.client.player.experienceLevel >= cost) || this.client.player.getAbilities().creativeMode;

        // Generate textures dynamically
        Identifier enabledTex;
        Identifier highlightedTex;
        Identifier disabledTex;

        if (usePlain) {
            enabledTex = PLAIN_SOURCE_ENABLED_TEXTURE;
            highlightedTex = PLAIN_SOURCE_HIGHLIGHTED_TEXTURE;
            disabledTex = PLAIN_SOURCE_DISABLED_TEXTURE;
        } else {
            enabledTex = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/source_" + texIndex + ".png");
            highlightedTex = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/source_" + texIndex + ".png");
            disabledTex = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/source_" + texIndex + ".png");
        }

        this.drawEnchantmentSlot(
            context, buttonX, buttonY, mouseX, mouseY,
            enchantingPower, cost, affordable, false,
            enabledTex,
            highlightedTex,
            disabledTex,
            enchantment.getName(level), // Pass the formatted name
            ENCHANTING_FROM_SOURCE_TEXT_ENABLED_COLOR,
            ENCHANTING_FROM_SOURCE_TEXT_HIGHLIGHTED_COLOR,
            ENCHANTING_FROM_SOURCE_TEXT_DISABLED_COLOR
        );
    }

    /**
     * Draws an "Apply" slot (source == TABLE).
     * This slot can be in an enabled, highlighted, or disabled state.
     *
     * @param context The draw context.
     * @param accessor The screen handler accessor for getting data.
     * @param buttonIndex The index of the slot (0-2).
     * @param buttonX The X-coordinate for the slot.
     * @param buttonY The Y-coordinate for the slot.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param lapisCount The player's current lapis count.
     * @param enchantingPower The level requirement for this slot.
     * @param enchantment The enchantment being displayed.
     * @param level The level of the enchantment.
     * @param texIndex The texture index (0-9) to use.
     * @param usePlain Whether to use plain textures.
     */
    @Unique
    private void drawApplySlot(DrawContext context, EnchantmentScreenHandlerAccessor accessor, int buttonIndex, int buttonX, int buttonY, int mouseX, int mouseY, int lapisCount, int enchantingPower, Enchantment enchantment, int level, int texIndex, boolean usePlain) {
        int cost = accessor.calculateEnchantmentCost(enchantment);
        // Affordability check for transfer/apply (uses enchantingPower for lapis)
        boolean affordable = (lapisCount >= cost && this.client.player.experienceLevel >= enchantingPower && this.client.player.experienceLevel >= cost) || this.client.player.getAbilities().creativeMode;

        // Generate textures dynamically
        Identifier enabledTex;
        Identifier highlightedTex;
        Identifier disabledTex;

        if (usePlain) {
            enabledTex = PLAIN_TABLE_ENABLED_TEXTURE;
            highlightedTex = PLAIN_TABLE_HIGHLIGHTED_TEXTURE;
            disabledTex = PLAIN_TABLE_DISABLED_TEXTURE;
        } else {
            enabledTex = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/table_" + texIndex + ".png");
            highlightedTex = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/table_" + texIndex + ".png");
            disabledTex = new Identifier(EnchantingOverhauled.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/table_" + texIndex + ".png");
        }

        // Get the original enchantment name
        Text originalName = enchantment.getName(level);
        // Get the original style and add the 'minecraft:alt' font
        // (This assumes GALACTIC_FONT_ID is defined in your class)
        Style galacticStyle = originalName.getStyle().withFont(GALACTIC_FONT_ID);
        // Create the new text component with the applied style
        Text galacticName = originalName.copy().setStyle(galacticStyle);

        Text name = Config.OBFUSCATE_NEW_ENCHANTMENTS
                    ? galacticName
                    : originalName;

        this.drawEnchantmentSlot(
                context, buttonX, buttonY, mouseX, mouseY,
                enchantingPower, cost, affordable, false,
                enabledTex,
                highlightedTex,
                disabledTex,
                name,
                ENCHANTING_FROM_TABLE_TEXT_ENABLED_COLOR,
                ENCHANTING_FROM_TABLE_TEXT_HIGHLIGHTED_COLOR,
                ENCHANTING_FROM_TABLE_TEXT_DISABLED_COLOR
        );
    }

    /**
     * Draws a generic enchantment slot that is not maxed out.
     * This is the base logic for upgrade, transfer, and apply slots, handling
     * texture selection, text rendering, and cost icon display based on state.
     *
     * @param context The draw context.
     * @param buttonX The X-coordinate for the slot.
     * @param buttonY The Y-coordinate for the slot.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param enchantingPower The level requirement for this slot.
     * @param cost The calculated XP/Lapis cost (1-3).
     * @param affordable Whether the player can afford this action.
     * @param isMaxed Whether the enchantment is maxed (should always be false here).
     * @param enabledTex The texture to use when the slot is enabled and not hovered.
     * @param highlightedTex The texture to use when the slot is hovered.
     * @param disabledTex The texture to use when the slot is disabled.
     * @param enchantmentName The fully formatted name of the enchantment to display.
     */
    @Unique
    private void drawEnchantmentSlot(DrawContext context, int buttonX, int buttonY, int mouseX, int mouseY,
                                     int enchantingPower, int cost, boolean affordable, boolean isMaxed,
                                     Identifier enabledTex, Identifier highlightedTex, Identifier disabledTex,
                                     Text enchantmentName,
                                     // ADDED PARAMETERS
                                     int enabledColor, int highlightedColor, int disabledColor) {

        int costIndex = Math.max(0, cost - 1); // Cost is 1-3, index is 0-2
        int textX = buttonX + ENCHANTING_TEXT_X_OFFSET;
        int textY = buttonY + ENCHANTING_TEXT_Y_OFFSET;

        String string = "" + enchantingPower;
        int enchantingPowerX = ENCHANTING_POWER_X_OFFSET - this.textRenderer.getWidth(string);
        int colorCode;

        if (!affordable) {
            RenderSystem.enableBlend();
            context.drawTexture(disabledTex, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);
            context.drawGuiTexture(LEVEL_DISABLED_TEXTURES[costIndex], buttonX + ENCHANTING_COST_X_OFFSET, buttonY + ENCHANTING_COST_Y_OFFSET, ENCHANTING_COST_WIDTH, ENCHANTING_COST_HEIGHT);
            RenderSystem.disableBlend();

            // Draw the enchantment name
            context.drawTextWrapped(this.textRenderer, enchantmentName, textX, textY, ENCHANTING_TEXT_MAX_WIDTH, disabledColor);

            colorCode = ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR; // disabled num color
        } else {
            int mouseDiffX = mouseX - buttonX;
            int mouseDiffY = mouseY - buttonY;
            boolean hovering = mouseDiffX >= 0 && mouseDiffY >= 0 && mouseDiffX < ENCHANTING_BUTTON_WIDTH && mouseDiffY < ENCHANTING_BUTTON_HEIGHT;
            RenderSystem.enableBlend();

            int nameColor; // Color for the enchantment name

            if (hovering) {
                context.drawTexture(highlightedTex, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);
                nameColor = highlightedColor;
            } else {
                context.drawTexture(enabledTex, buttonX, buttonY, 0, 0, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT);
                nameColor = enabledColor;
            }

            context.drawGuiTexture(LEVEL_TEXTURES[costIndex], buttonX + ENCHANTING_COST_X_OFFSET, buttonY + ENCHANTING_COST_Y_OFFSET, ENCHANTING_COST_WIDTH, ENCHANTING_COST_HEIGHT);
            RenderSystem.disableBlend();

            // Draw the enchantment name
            context.drawTextWrapped(this.textRenderer, enchantmentName, textX, textY, ENCHANTING_TEXT_MAX_WIDTH, nameColor);

            colorCode = ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR; // default num color
        }

        // Hide Level Requirement if Maxed
        int outlineColor = 0;
        if (!isMaxed) {
            // Draw outline
            context.drawText(this.textRenderer, string, textX + ENCHANTING_POWER_X_OFFSET - this.textRenderer.getWidth(string) + 1, buttonY + ENCHANTING_POWER_Y_OFFSET, outlineColor, false);
            context.drawText(this.textRenderer, string, textX + ENCHANTING_POWER_X_OFFSET - this.textRenderer.getWidth(string) - 1, buttonY + ENCHANTING_POWER_Y_OFFSET, outlineColor, false);
            context.drawText(this.textRenderer, string, textX + ENCHANTING_POWER_X_OFFSET - this.textRenderer.getWidth(string), buttonY + ENCHANTING_POWER_Y_OFFSET + 1, outlineColor, false);
            context.drawText(this.textRenderer, string, textX + ENCHANTING_POWER_X_OFFSET - this.textRenderer.getWidth(string), buttonY + ENCHANTING_POWER_Y_OFFSET - 1, outlineColor, false);

            // Draw level requirement
            context.drawText(
                this.textRenderer,
                string,
                textX + ENCHANTING_POWER_X_OFFSET - this.textRenderer.getWidth(string),
                buttonY + ENCHANTING_POWER_Y_OFFSET,
                colorCode,
                false
            );
            context.drawTextWithShadow(this.textRenderer, string, textX + ENCHANTING_POWER_X_OFFSET - this.textRenderer.getWidth(string), buttonY + ENCHANTING_POWER_Y_OFFSET, colorCode);
        }
    }

    /**
     * Draws a slot that is "Maxed Out" (source == TARGET and level >= maxLevel)
     * or "Over-Enchanted" (level > maxLevel).
     * This slot is non-interactive and displays the enchantment's name
     * centered within the button.
     *
     * @param context The draw context.
     * @param enchantment The enchantment that is maxed.
     * @param level The (max) level of the enchantment.
     * @param buttonX The X-coordinate for the slot.
     * @param buttonY The Y-coordinate for the slot.
     */
    @Unique
    private void drawMaxedSlot(DrawContext context, Enchantment enchantment, int level, int buttonX, int buttonY) {
        // 1. Get the fully formatted name
        Text enchantmentName = enchantment.getName(level);

        // 2. Calculate coordinates
        int textY = buttonY + ENCHANTING_TEXT_Y_OFFSET;
        int textWidth = this.textRenderer.getWidth(enchantmentName);
        int textX = buttonX + 8;

        // 3. Draw the background texture
        Identifier backgroundTexture = (level > enchantment.getMaxLevel()) ? ENCHANTMENT_SLOT_OVER_ENCHANTED_TEXTURE : ENCHANTMENT_SLOT_MAXED_OUT_TEXTURE;
        context.drawTexture(
            backgroundTexture,
            buttonX,
            buttonY,
            0,
            0,
            ENCHANTING_BUTTON_WIDTH,
            ENCHANTING_BUTTON_HEIGHT,
            ENCHANTING_BUTTON_WIDTH,
            ENCHANTING_BUTTON_HEIGHT
        );

        // 4. Draw the text, centered, with no shadow.
        int color = (level > enchantment.getMaxLevel()) ? ENCHANTMENT_OVER_ENCHANTED_TEXT_COLOR : ENCHANTMENT_MAXED_OUT_TEXT_COLOR;
        context.drawText(
            this.textRenderer,
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
     *
     * @param context The draw context.
     * @param accessor The screen handler accessor for getting data.
     * @param alignX The X-coordinate of the screen's top-left corner.
     * @param alignY The Y-coordinate of the screen's top-left corner.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param lapisCount The player's current lapis count.
     * @param enchantmentSources The array of enchantment sources.
     */
    @Unique
    private void drawRerollButton(DrawContext context, EnchantmentScreenHandlerAccessor accessor, int alignX, int alignY, int mouseX, int mouseY, int lapisCount, int[] enchantmentSources) {
        // Get Reroll Button Logic
        ItemStack target = accessor.getEnchantmentTarget();
        boolean targetIsEmpty = target.isEmpty();
        boolean targetIsEnchantable = !targetIsEmpty && (target.isOf(Items.BOOK) || target.isEnchantable());
        boolean targetIsSourceEnchantable = Arrays.stream(enchantmentSources).anyMatch(element -> element == SOURCE);

        ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
        int occupiedSlots = curseFreeTarget.getEnchantments().getSize();

        int rerollCost = occupiedSlots + 1;
        int costIndex = Math.max(0, rerollCost - 1); // Clamp index
        boolean hasTableSource = Arrays.stream(enchantmentSources).anyMatch(source -> source == TABLE);
        boolean canReroll = occupiedSlots < 3 && !targetIsSourceEnchantable && targetIsEnchantable && hasTableSource;

        int x = alignX + REROLL_BUTTON_X_OFFSET;
        int y = alignY + REROLL_BUTTON_Y_OFFSET;

        RenderSystem.enableBlend();

        boolean cannotAfford = (lapisCount < rerollCost || this.client.player.experienceLevel < rerollCost) && !this.client.player.getAbilities().creativeMode;

        boolean rerollEnabled = !targetIsEmpty && targetIsEnchantable && canReroll && !cannotAfford;

        if (rerollEnabled) {
            // Draw enabled/highlighted
            // Draw enabled/highlighted
            int mouseXdiff = mouseX - x;
            int mouseYdiff = mouseY - y;

            if (mouseXdiff >= 0 && mouseYdiff >= 0 && mouseXdiff < REROLL_BUTTON_WIDTH && mouseYdiff < REROLL_BUTTON_HEIGHT) {
                context.drawTexture(REROLL_HIGHLIGHTED_TEXTURE, x, y, 0, 0, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT);
            } else {
                context.drawTexture(REROLL_TEXTURE, x, y, 0, 0, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT);
            }
            // costIndex is safe because rerollEnabled -> canReroll -> occupiedSlots < 3
            if (costIndex < LEVEL_TEXTURES.length) {
                context.drawGuiTexture(LEVEL_TEXTURES[costIndex], x + REROLL_COST_X_OFFSET, y + REROLL_COST_Y_OFFSET, REROLL_COST_WIDTH, REROLL_COST_HEIGHT);
            }
        } else {
            // Draw disabled
            context.drawTexture(REROLL_DISABLED_TEXTURE, x, y, 0, 0, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT);

            // Show disabled cost only if the *only* reason it's disabled is cost
            boolean showDisabledCost = !targetIsEmpty && targetIsEnchantable && canReroll && cannotAfford;
            if (showDisabledCost && costIndex < LEVEL_DISABLED_TEXTURES.length) {
                // costIndex is safe because showDisabledCost -> canReroll -> occupiedSlots < 3
                context.drawGuiTexture(LEVEL_DISABLED_TEXTURES[costIndex], x + REROLL_COST_X_OFFSET, y + REROLL_COST_Y_OFFSET, REROLL_COST_WIDTH, REROLL_COST_HEIGHT);
            }
        }
        RenderSystem.disableBlend();
    }
    // endregion

    // region Draw Experience Bar
    /**
     * Helper method to draw the player's experience bar and level number.
     * This replaces the logic previously handled by {@code drawExperienceBar}.
     *
     * @param context The draw context.
     * @param alignX The X-coordinate of the screen's top-left corner.
     * @param alignY The Y-coordinate of the screen's top-left corner.
     */
    @Unique
    private void drawExperienceBar(DrawContext context, int alignX, int alignY) {
        // 1. Define Bar Geometry
        int barX = alignX + EXPERIENCE_BAR_X_OFFSET;
        int barY = alignY + EXPERIENCE_BAR_Y_OFFSET;
        int barWidth = EXPERIENCE_BAR_WIDTH;
        int barHeight = EXPERIENCE_BAR_HEIGHT;

        // 2. Draw Inactive Bar
        // We draw the full inactive bar first as a background.
        // Assuming the texture file is 102x5
        context.drawTexture(EXPERIENCE_BAR_INACTIVE, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        // 3. Draw Active Bar
        float progress = this.client.player.experienceProgress;
        int activeWidth = (int)(progress * (float) barWidth); // Calculate fill width

        // Draw the active bar, cropped to the calculated width
        if (activeWidth > 0) {
            context.drawTexture(EXPERIENCE_BAR_ACTIVE, barX, barY, 0, 0, activeWidth, barHeight, barWidth, barHeight);
        }

        // 4. Draw Level Number
        int playerLevel = this.client.player.experienceLevel;
        if (playerLevel > 0) {
            String levelString = "" + playerLevel;
            int textWidth = this.textRenderer.getWidth(levelString);

            // Center the text horizontally over the
            // Bar's center X = barX + (barWidth / 2)
            // Text's X = Bar's center X - (textWidth / 2)
            int textX = barX + (barWidth / 2) - (textWidth / 2);
            int textY = alignY + EXPERIENCE_BAR_Y_OFFSET - 6;

            // Draw the black outline
            int outlineColor = 0; // Black
            context.drawText(this.textRenderer, levelString, textX + 1, textY, outlineColor, false); // Right
            context.drawText(this.textRenderer, levelString, textX - 1, textY, outlineColor, false); // Left
            context.drawText(this.textRenderer, levelString, textX, textY + 1, outlineColor, false); // Down
            context.drawText(this.textRenderer, levelString, textX, textY - 1, outlineColor, false); // Up

            // Draw the main text
            context.drawText(
                    this.textRenderer,
                    levelString,
                    textX,
                    textY,
                    ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR, // 8453920
                    false // No shadow
            );
        }
    } // endregion

    // region Interaction handler
    /**
     * Overrides the mouse click handler to process clicks on the new button layout.
     * <p>
     * This is necessary because the {@code backgroundHeight} (and thus {@code y})
     * has changed, which would break the vanilla coordinate calculations.
     *
     * @param mouseX Mouse X position.
     * @param mouseY Mouse Y position.
     * @param button Mouse button clicked.
     * @return true if the click was handled, false otherwise.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Use the new backgroundHeight (186) to calculate 'alignY'
        int alignX = this.x;
        int alignY = this.y;

        // Check enchantment buttons (0-2)
        for (int buttonIndex = 0; buttonIndex < REROLL_BUTTON_INDEX; ++buttonIndex) {
            double mouseDiffX = mouseX - (double)(alignX + ENCHANTING_BUTTON_X_OFFSET);
            double mouseDiffY = mouseY - (double)(alignY + ENCHANTING_BUTTON_Y_OFFSET + ENCHANTING_BUTTON_HEIGHT * buttonIndex); // Uses new 'alignY'
            if (mouseDiffX >= 0.0F && mouseDiffY >= 0.0F && mouseDiffX < ENCHANTING_BUTTON_WIDTH && mouseDiffY < ENCHANTING_BUTTON_HEIGHT && ((EnchantmentScreenHandler) this.handler).onButtonClick(this.client.player, buttonIndex)) {
                this.client.interactionManager.clickButton(((EnchantmentScreenHandler) this.handler).syncId, buttonIndex);
                return true;
            }
        }

        // Check reroll button (3)
        double mouseXdiff = mouseX - (double)(alignX + REROLL_BUTTON_X_OFFSET);
        double mouseYdiff = mouseY - (double)(alignY + REROLL_BUTTON_Y_OFFSET); // Uses new 'alignY'

        if (mouseXdiff >= 0.0F && mouseYdiff >= 0.0F && mouseXdiff < (double) REROLL_BUTTON_WIDTH && mouseYdiff < (double) REROLL_BUTTON_HEIGHT && this.handler.onButtonClick(this.client.player, REROLL_BUTTON_INDEX)) {
            this.client.interactionManager.clickButton(this.handler.syncId, REROLL_BUTTON_INDEX);
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
     * @param mouseX Mouse X position.
     * @param mouseY Mouse Y position.
     * @param delta Frame delta time.
     * @param ci Callback info to cancel the original method.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void overrideRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel(); // Cancel the vanilla render method entirely

        // 1. This is the body of super.render()
        // It will call our injected drawCustomBackground()
        super.render(context, mouseX, mouseY, delta);

        // 2. This is the vanilla call for item tooltips
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        // 3. Get common data for helpers
        boolean isCreative = this.client.player.getAbilities().creativeMode;
        int lapisCount = ((EnchantmentScreenHandler) this.handler).getLapisCount();
        EnchantmentScreenHandlerAccessor accessor = (EnchantmentScreenHandlerAccessor) this.handler;

        // 4. Try drawing enchantment slot tooltips
        // This helper returns true if it drew a tooltip, so we can stop.
        boolean tooltipDrawn = this.drawEnchantmentSlotTooltips(context, mouseX, mouseY, accessor, lapisCount, isCreative);

        // 5. If no enchantment tooltip was drawn, try drawing the reroll tooltip
        if (!tooltipDrawn) {
            this.drawRerollButtonTooltip(context, mouseX, mouseY, accessor, lapisCount, isCreative);
        }
    }

    /**
     * Draws the tooltips for the three enchantment slots (0-2) by dispatching
     * to the correct helper method based on the enchantment's source.
     *
     * @param context The draw context.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param accessor The screen handler accessor.
     * @param lapisCount The player's lapis count.
     * @param isCreative Whether the player is in creative mode.
     * @return {@code true} if a tooltip was drawn, {@code false} otherwise.
     */
    @Unique
    private boolean drawEnchantmentSlotTooltips(DrawContext context, int mouseX, int mouseY, EnchantmentScreenHandlerAccessor accessor, int lapisCount, boolean isCreative) {
        int[] enchantmentSources = accessor.getEnchantmentSourceArray();

        for (int buttonIndex = 0; buttonIndex < REROLL_BUTTON_INDEX; ++buttonIndex) {
            int powerRequirement = ((EnchantmentScreenHandler) this.handler).enchantmentPower[buttonIndex];
            Enchantment enchantment = Enchantment.byRawId(((EnchantmentScreenHandler) this.handler).enchantmentId[buttonIndex]);
            int enchantmentLevel = ((EnchantmentScreenHandler) this.handler).enchantmentLevel[buttonIndex];
            int source = enchantmentSources[buttonIndex];

            // Use the CORRECT coordinates from our constants
            int buttonY = ENCHANTING_BUTTON_Y_OFFSET + (ENCHANTING_BUTTON_HEIGHT * buttonIndex);

            // Check if hovered and the slot is valid
            if (this.isPointWithinBounds(ENCHANTING_BUTTON_X_OFFSET, buttonY, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT, (double) mouseX, (double) mouseY) && powerRequirement > 0 && enchantmentLevel >= 0 && enchantment != null) {

                // Check if this is an upgrade slot that is already maxed
                boolean isMaxed = (source == TARGET && enchantmentLevel >= enchantment.getMaxLevel());

                if (isMaxed) {
                    // Case 1: Maxed/Over-leveled Upgrade (No Title, No Cost)
                    this.drawMaxedTooltip(context, mouseX, mouseY, enchantment, enchantmentLevel);
                    return true;
                } else if (source == TARGET) {
                    // Case 2: Standard Upgrade (Title + Cost)
                    this.drawUpgradeTooltip(context, mouseX, mouseY, accessor, lapisCount, isCreative, enchantment, enchantmentLevel, powerRequirement);
                    return true;
                } else if (source == SOURCE) {
                    // Case 3: Transfer (Title + Cost)
                    this.drawTransferTooltip(context, mouseX, mouseY, accessor, lapisCount, isCreative, enchantment, enchantmentLevel, powerRequirement);
                    return true;
                } else if (source == TABLE) {
                    // Case 4: Apply (Title + Cost + Obfuscation)
                    this.drawApplyTooltip(context, mouseX, mouseY, accessor, lapisCount, isCreative, enchantment, enchantmentLevel, powerRequirement);
                    return true;
                }
            }
        }
        return false; // No tooltip was drawn
    }

    /**
     * Draws the tooltip for a "Maxed Out" or "Over-Leveled" (TARGET) slot.
     * This version has no title and no cost information.
     *
     * @param context The draw context.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param enchantment The enchantment to display.
     * @param enchantmentLevel The level of the enchantment.
     */
    @Unique
    private void drawMaxedTooltip(DrawContext context, int mouseX, int mouseY, Enchantment enchantment, int enchantmentLevel) {
        java.util.List<OrderedText> list = com.google.common.collect.Lists.newArrayList();

        // Add the enchantment name
        list.add(enchantment.getName(enchantmentLevel).copy().asOrderedText());
        // list.add(net.minecraft.screen.ScreenTexts.EMPTY.asOrderedText()); // REMOVED

        // Add enchantment description
        String descKey = enchantment.getTranslationKey() + ".desc";
        Text description = Text.translatable(descKey).formatted(Formatting.GRAY);
        list.addAll(EnchantmentLib.wrapDescription(description));

        context.drawOrderedTooltip(this.textRenderer, list, mouseX, mouseY);
    }

    /**
     * Draws the tooltip for a standard, non-maxed "Upgrade" (TARGET) slot.
     *
     * @param context The draw context.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param accessor The screen handler accessor.
     * @param lapisCount The player's lapis count.
     * @param isCreative Whether the player is in creative mode.
     * @param enchantment The enchantment to display.
     * @param enchantmentLevel The level of the enchantment.
     * @param powerRequirement The level requirement.
     */
    @Unique
    private void drawUpgradeTooltip(DrawContext context, int mouseX, int mouseY, EnchantmentScreenHandlerAccessor accessor, int lapisCount, boolean isCreative, Enchantment enchantment, int enchantmentLevel, int powerRequirement) {
        int cost = accessor.calculateEnchantmentCost(enchantment);
        java.util.List<OrderedText> list = com.google.common.collect.Lists.newArrayList();

        // Add "Upgrade:" title
        list.add(Text.translatable("gui.enchanting_overhauled.upgrade").formatted(Formatting.WHITE).asOrderedText());

        // Add the enchantment name
        list.add(enchantment.getName(enchantmentLevel).copy().asOrderedText());

        // Add enchantment description (indented via lib method)
        String descKey = enchantment.getTranslationKey() + ".desc";
        Text description = Text.translatable(descKey).formatted(Formatting.GRAY); // UPDATED (manual indent removed)
        list.addAll(EnchantmentLib.wrapDescription(description)); // UPDATED

        // Add cost information
        this.drawTooltipCost(list, isCreative, powerRequirement, cost, lapisCount);

        context.drawOrderedTooltip(this.textRenderer, list, mouseX, mouseY);
    }

    /**
     * Draws the tooltip for a "Transfer" (SOURCE) slot.
     *
     * @param context The draw context.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param accessor The screen handler accessor.
     * @param lapisCount The player's lapis count.
     * @param isCreative Whether the player is in creative mode.
     * @param enchantment The enchantment to display.
     * @param enchantmentLevel The level of the enchantment.
     * @param powerRequirement The level requirement.
     */
    @Unique
    private void drawTransferTooltip(DrawContext context, int mouseX, int mouseY, EnchantmentScreenHandlerAccessor accessor, int lapisCount, boolean isCreative, Enchantment enchantment, int enchantmentLevel, int powerRequirement) {
        int cost = accessor.calculateEnchantmentCost(enchantment);
        java.util.List<OrderedText> list = com.google.common.collect.Lists.newArrayList();

        // Add "Transfer:" title
        list.add(Text.translatable("gui.enchanting_overhauled.transfer").formatted(Formatting.WHITE).asOrderedText());

        // Add the enchantment name
        list.add(enchantment.getName(enchantmentLevel).copy().asOrderedText());

        // Add enchantment description (indented via lib method)
        String descKey = enchantment.getTranslationKey() + ".desc";
        Text description = Text.translatable(descKey).formatted(Formatting.GRAY); // UPDATED (manual indent removed)
        list.addAll(EnchantmentLib.wrapDescription(description)); // UPDATED

        // Add cost information
        this.drawTooltipCost(list, isCreative, powerRequirement, cost, lapisCount);

        context.drawOrderedTooltip(this.textRenderer, list, mouseX, mouseY);
    }

    /**
     * Draws the tooltip for an "Apply" (TABLE) slot.
     * Adds an "Apply:" title and obfuscates text based on config.
     * Applies the enchantment's theme color to its name.
     *
     * @param context The draw context.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param accessor The screen handler accessor.
     * @param lapisCount The player's lapis count.
     * @param isCreative Whether the player is in creative mode.
     * @param enchantment The enchantment to display.
     * @param enchantmentLevel The level of the enchantment.
     * @param powerRequirement The level requirement.
     */
    @Unique
    private void drawApplyTooltip(DrawContext context, int mouseX, int mouseY, EnchantmentScreenHandlerAccessor accessor, int lapisCount, boolean isCreative, Enchantment enchantment, int enchantmentLevel, int powerRequirement) {
        int cost = accessor.calculateEnchantmentCost(enchantment);
        java.util.List<OrderedText> list = com.google.common.collect.Lists.newArrayList();

        // --- Get Theme Color ---
        EnchantmentThemeAccessor themeAccessor = (EnchantmentThemeAccessor) enchantment;
        RegistryKey<EnchantmentTheme> themeKey = themeAccessor.getTheme();
        Registry<EnchantmentTheme> themeRegistry = this.client.world.getRegistryManager().get(EnchantmentThemeRegistry.THEME_REGISTRY_KEY);
        EnchantmentTheme theme = themeRegistry.get(themeKey);

        // Get the color from the theme, or default to white (0xFFFFFF)
        int color = 0xFFFFFF;
        if (theme != null) {
            color = theme.colorCode().orElse(0xFFFFFF);
        }
        // --- End Get Theme Color ---

        // --- Text Obfuscation Logic ---
        Text enchantmentName = enchantment.getName(enchantmentLevel);
        String descKey = enchantment.getTranslationKey() + ".desc";

        // Create the base description (formatted)
        Text description = Text.translatable(descKey).formatted(Formatting.GRAY);
        // Create the base title (formatted)
        Text title = Text.translatable("gui.enchanting_overhauled.apply").formatted(Formatting.WHITE);

        if (Config.OBFUSCATE_NEW_ENCHANTMENTS) {
            Style galacticStyle = Style.EMPTY.withFont(GALACTIC_FONT_ID);
            enchantmentName = enchantmentName.copy().setStyle(enchantmentName.getStyle().withFont(GALACTIC_FONT_ID)).withColor(color);
            description = description.copy().setStyle(description.getStyle().withFont(GALACTIC_FONT_ID)).formatted(Formatting.GRAY);
        }
        // --- End of Text Obfuscation Logic ---

        // Add the (potentially obfuscated) title
        list.add(title.asOrderedText());

        // Add the (potentially obfuscated) enchantment name
        list.add(enchantmentName.asOrderedText());

        // Add the (potentially obfuscated) enchantment description
        list.addAll(EnchantmentLib.wrapDescription(description));

        // Add cost information
        this.drawTooltipCost(list, isCreative, powerRequirement, cost, lapisCount);

        context.drawOrderedTooltip(this.textRenderer, list, mouseX, mouseY);
    }

    /**
     * Appends the standardized cost information (level, lapis) to a tooltip list.
     *
     * @param list The list of OrderedText to append to.
     * @param isCreative Whether the player is in creative mode.
     * @param powerRequirement The level requirement.
     * @param cost The lapis/XP cost (1-3).
     * @param lapisCount The player's current lapis count.
     */
    @Unique
    private void drawTooltipCost(java.util.List<OrderedText> list, boolean isCreative, int powerRequirement, int cost, int lapisCount) {
        // A 'TABLE' enchantment can't be 'isMaxed', so we only check 'isCreative'
        if (!isCreative) {
            list.add(net.minecraft.screen.ScreenTexts.EMPTY.asOrderedText());
            if (this.client.player.experienceLevel < powerRequirement) {
                // Level requirement
                list.add(Text.translatable("container.enchant.level.requirement", new Object[]{powerRequirement}).formatted(net.minecraft.util.Formatting.RED).asOrderedText());
            } else {
                // Lapis requirement
                net.minecraft.text.MutableText mutableText;
                if (cost == 1) {
                    mutableText = Text.translatable("container.enchant.lapis.one");
                } else {
                    mutableText = Text.translatable("container.enchant.lapis.many", new Object[]{cost});
                }
                list.add(mutableText.formatted(lapisCount >= cost ? net.minecraft.util.Formatting.GRAY : net.minecraft.util.Formatting.RED).asOrderedText());

                // Level cost (for information)
                net.minecraft.text.MutableText mutableText2;
                if (cost == 1) {
                    mutableText2 = Text.translatable("container.enchant.level.one");
                } else {
                    mutableText2 = Text.translatable("container.enchant.level.many", new Object[]{cost});
                }
                list.add(mutableText2.formatted(net.minecraft.util.Formatting.GRAY).asOrderedText());
            }
        }
    }

    /**
     * Draws the tooltip for the reroll button (3).
     *
     * @param context The draw context.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param accessor The screen handler accessor.
     * @param lapisCount The player's lapis count.
     * @param isCreative Whether the player is in creative mode.
     */
    @Unique
    private void drawRerollButtonTooltip(DrawContext context, int mouseX, int mouseY, EnchantmentScreenHandlerAccessor accessor, int lapisCount, boolean isCreative) {
        if (this.isPointWithinBounds(REROLL_BUTTON_X_OFFSET, REROLL_BUTTON_Y_OFFSET, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT, (double) mouseX, (double) mouseY)) {
            // Check reroll validity
            int[] enchantmentSources = accessor.getEnchantmentSourceArray();
            ItemStack target = accessor.getEnchantmentTarget();
            boolean targetIsEmpty = target.isEmpty();
            boolean targetIsEnchantable = !targetIsEmpty && (target.isOf(Items.BOOK) || target.isEnchantable());
            boolean targetIsSourceEnchantable = Arrays.stream(enchantmentSources).anyMatch(element -> element == SOURCE);
            ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
            int occupiedSlots = curseFreeTarget.getEnchantments().getSize();
            int rerollCost = occupiedSlots + 1;
            boolean canReroll = occupiedSlots < 3 && !targetIsSourceEnchantable && targetIsEnchantable;

            // Build tooltip
            java.util.List<OrderedText> list = com.google.common.collect.Lists.newArrayList();
            // Assuming you have this translation key for "Turn Page"
            list.add(Text.translatable("gui.enchanting_overhauled.turn_page").formatted(net.minecraft.util.Formatting.WHITE).asOrderedText());

            // REFACTORED:
            // Delegate cost drawing to the generic helper.
            // For a reroll, the 'powerRequirement' and the 'cost' are the same.
            if (!targetIsEmpty && canReroll) {
                this.drawTooltipCost(list, isCreative, rerollCost, rerollCost, lapisCount);
            }

            context.drawOrderedTooltip(this.textRenderer, list, mouseX, mouseY);
        }
    } // endregion
}