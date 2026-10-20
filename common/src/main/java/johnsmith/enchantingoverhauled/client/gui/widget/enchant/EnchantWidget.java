package johnsmith.enchantingoverhauled.client.gui.widget.enchant;

import com.mojang.blaze3d.systems.RenderSystem;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import johnsmith.enchantingoverhauled.client.UIFormattingUtil;
import johnsmith.enchantingoverhauled.api.config.type.ColoringMode;
import johnsmith.enchantingoverhauled.api.config.type.FormatApplicationMode;
import johnsmith.enchantingoverhauled.api.config.type.ObfuscationLevel;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantRenderState;

import johnsmith.enchantingoverhauled.menu.enchant.EnchantmentMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/**
 * The abstract base class representing a single row item (widget) in the Enchanting Altar's UI list.
 * <p>
 * Widgets in this system are stateful; their internal coordinates, data state, and execution callbacks
 * can be dynamically updated during pagination without needing to instantiate new objects,
 * reducing memory churn.
 * <p>
 * This class is sealed, strictly defining its functional states via static nested classes.
 */
public sealed abstract class EnchantWidget permits
        EnchantWidget.Empty,
        EnchantWidget.Available,
        EnchantWidget.Locked,
        EnchantWidget.Maxed,
        EnchantWidget.Overleveled
{

    // region Constants
    /** The font identifier for the standard galactic alphabet used for obfuscation. */
    protected static final ResourceLocation GALACTIC_FONT_ID = ResourceLocation.fromNamespaceAndPath("minecraft", "alt");

    /** Cost indicator sprites for active, affordable enchantments. */
    protected static final ResourceLocation[] ENABLED_LEVEL_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3")};

    /** Cost indicator sprites for disabled, unaffordable, or locked enchantments. */
    protected static final ResourceLocation[] DISABLED_LEVEL_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1_disabled"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2_disabled"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3_disabled")};
    // endregion

    // region Fields
    /** The configuration mapping the dimensions and internal offsets for rendering text and sprites. */
    protected final WidgetLayoutConfig layout = WidgetLayoutConfig.DEFAULT;

    protected int x;
    protected int y;
    protected EnchantRenderState state;
    protected int texIndex;
    protected final boolean usePlainBackground;
    protected Runnable onPressAction;

    /** Tracks if the mouse pointer is currently positioned over this widget bounds. */
    public boolean isHovered;
    // endregion

    // region Helpers
    /**
     * Formats the underlying enchantment name, resolving theme coloring and obfuscation dynamically.
     */
    protected MutableComponent getFormattedEnchantmentName(EnchantmentSource source) {
        if (this.state.enchantment().isEmpty()) return Component.empty();
        ObfuscationLevel obsLevel = Config.OBFUSCATION_LEVEL.get();
        boolean obfuscate = obsLevel == ObfuscationLevel.FULL || (obsLevel == ObfuscationLevel.UNTAGGED && source == EnchantmentSource.TABLE);

        FormatApplicationMode mode = Config.ENCHANTMENT_MENU_FORMAT_APPLICATION_MODE.get();
        ColoringMode themeColoring = (mode == FormatApplicationMode.WIDGET || mode == FormatApplicationMode.ALL) ? Config.ENCHANTMENT_MENU_THEME_COLORING.get() : ColoringMode.NONE;
        ColoringMode levelColoring = (mode == FormatApplicationMode.WIDGET || mode == FormatApplicationMode.ALL) ? Config.ENCHANTMENT_MENU_LEVEL_COLORING.get() : ColoringMode.NONE;
        boolean showFirstLevel = (mode == FormatApplicationMode.WIDGET || mode == FormatApplicationMode.ALL) && Config.ENCHANTMENT_MENU_SHOW_FIRST_LEVEL.get();

        return UIFormattingUtil.formatEnchantmentName(this.state.enchantment().get(), this.state.displayLevel(), obfuscate, themeColoring, levelColoring, showFirstLevel);
    }

    /**
     * Renders a marquee scrolling string if the text width exceeds the available max width bounding box.
     */
    protected void drawScrollingString(GuiGraphics guiGraphics, Font font, Component text, int minX, int minY, int maxX, int color) {
        int textWidth = font.width(text);
        int maxWidth = maxX - minX;

        if (textWidth > maxWidth) {
            int overflow = textWidth - maxWidth;
            double time = (double) net.minecraft.Util.getMillis() / 1000.0D;
            double speed = Math.max((double) overflow * 0.5D, 3.0D);
            double scrollPos = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * time / speed)) / 2.0D + 0.5D;
            double offset = net.minecraft.util.Mth.lerp(scrollPos, 0.0D, (double) overflow);

            guiGraphics.enableScissor(minX, minY - 2, maxX, minY + font.lineHeight + 2);
            guiGraphics.drawString(font, text, minX - (int) offset, minY, color, false);
            guiGraphics.disableScissor();
        } else {
            guiGraphics.drawString(font, text, minX, minY, color, false);
        }
    }
    // endregion

    // region Initialization
    /**
     * Constructs a base EnchantWidget.
     *
     * @param x                  The initial X screen coordinate.
     * @param y                  The initial Y screen coordinate.
     * @param state              The rendering data state mapped to this slot.
     * @param texIndex           The texture variant index (0-9).
     * @param usePlainBackground Whether to use the untextured button variants.
     * @param onPressAction      The action callback triggered on successful click.
     */
    public EnchantWidget(int x, int y, EnchantRenderState state, int texIndex, boolean usePlainBackground, Runnable onPressAction) {
        this.x = x;
        this.y = y;
        this.state = state;
        this.texIndex = texIndex;
        this.usePlainBackground = usePlainBackground;
        this.onPressAction = onPressAction;
    }
    // endregion

    // region Accessors & Mutators
    /**
     * Retrieves the current rendering state encapsulated by this widget.
     *
     * @return The active {@link EnchantRenderState}.
     */
    public EnchantRenderState getState() {
        return this.state;
    }

    /**
     * Updates the underlying data state of this widget.
     *
     * @param state The new {@link EnchantRenderState}.
     */
    public void setState(EnchantRenderState state) {
        this.state = state;
    }

    /**
     * Updates the screen coordinates of this widget without re-instantiation.
     *
     * @param x The new X coordinate.
     * @param y The new Y coordinate.
     */
    public void updatePosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Updates the visual texture variant index used by the widget.
     *
     * @param texIndex The new variant index (typically 0-9).
     */
    public void setTexIndex(int texIndex) {
        this.texIndex = texIndex;
    }

    /**
     * Overwrites the callback execution block bound to a successful click event.
     *
     * @param onPressAction The new runnable task.
     */
    public void setOnPressAction(Runnable onPressAction) {
        this.onPressAction = onPressAction;
    }
    // endregion

    // region Rendering & Input
    /**
     * Evaluates the mouse hover state and delegates the actual drawing logic to the subclass.
     *
     * @param context     The GUI graphics context to draw to.
     * @param mouseX      The current mouse X coordinate.
     * @param mouseY      The current mouse Y coordinate.
     * @param partialTick The partial tick time for animations.
     */
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.layout.buttonWidth() && mouseY < this.y + this.layout.buttonHeight();
        this.renderWidget(context, mouseX, mouseY, partialTick);
    }

    /**
     * The internal rendering logic defined by the specific widget subclass.
     *
     * @param context     The GUI graphics context.
     * @param mouseX      The mouse X coordinate.
     * @param mouseY      The mouse Y coordinate.
     * @param partialTick The partial tick time.
     */
    protected abstract void renderWidget(GuiGraphics context, int mouseX, int mouseY, float partialTick);

    /**
     * Evaluates click events. Triggers the assigned action callback if the widget is hovered
     * and the primary mouse button (Left-click) is used.
     *
     * @param mouseX The X coordinate of the mouse pointer.
     * @param mouseY The Y coordinate of the mouse pointer.
     * @param button The mouse button pressed (0 for Left-Click).
     * @return true if the click was consumed, false otherwise.
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered && button == 0) {
            this.onPressAction.run();
            return true;
        }
        return false;
    }
    // endregion

    // region Empty
    /**
     * A non-interactable filler widget used to pad the Enchantment Screen's list view
     * when there are fewer active enchantment contexts available than the current page size.
     */
    public static final class Empty extends EnchantWidget {

        // region Initialization
        /**
         * Constructs a new EnchantWidget.Empty.
         *
         * @param x                  The X screen coordinate.
         * @param y                  The Y screen coordinate.
         * @param state              The state object (typically initialized to an empty default).
         * @param texIndex           The deterministic texture index to use.
         * @param usePlainBackground True if the plain background setting is enabled.
         * @param onPressAction      The action callback (unused in empty widgets).
         */
        public Empty(int x, int y, EnchantRenderState state, int texIndex, boolean usePlainBackground, Runnable onPressAction) {
            super(x, y, state, texIndex, usePlainBackground, onPressAction);
        }
        // endregion

        // region Rendering & Input
        /**
         * Denies all click interactions for empty widgets.
         *
         * @param mouseX The X coordinate of the mouse pointer.
         * @param mouseY The Y coordinate of the mouse pointer.
         * @param button The mouse button pressed.
         * @return Always false.
         */
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        /**
         * Renders a blank, disabled background texture as a placeholder.
         * Defaults to the Table Source's disabled texture style for aesthetic consistency.
         *
         * @param context     The GUI graphics context to draw to.
         * @param mouseX      The current mouse X coordinate.
         * @param mouseY      The current mouse Y coordinate.
         * @param partialTick The partial tick time for animations.
         */
        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
            // Fall back to the default table source texture to fill the blank space
            ResourceLocation disabledTex = this.usePlainBackground ? EnchantmentSource.TABLE.disabledTexture : EnchantmentSource.TABLE.getDisabledTexture(this.texIndex);
            RenderSystem.enableBlend();
            context.blit(disabledTex, this.x, this.y, 0, 0, this.layout.buttonWidth(), this.layout.buttonHeight(), this.layout.buttonWidth(), this.layout.buttonHeight());
            RenderSystem.disableBlend();
        }
        // endregion
    }
    // endregion

    // region Available
    /**
     * Represents a standard, interactable enchantment option within the Enchantment Screen.
     * <p>
     * This widget actively evaluates the player's current experience and lapis lazuli
     * to determine if the enchantment is affordable. If the player cannot afford the action,
     * the widget dynamically switches to a disabled visual state while remaining in the "Available"
     * structural category.
     */
    public static final class Available extends EnchantWidget {

        // region Constants
        private static final int ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR = 0x80FF20;
        private static final int ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR = 0x408000;
        // endregion

        // region Initialization
        /**
         * Constructs a new EnchantWidget.Available.
         *
         * @param x                  The X screen coordinate.
         * @param y                  The Y screen coordinate.
         * @param state              The visual and data state representing this specific enchantment.
         * @param texIndex           The deterministic texture index (0-9) used to select the randomized background variant.
         * @param usePlainBackground True if the user configuration requests plain backgrounds.
         * @param onPressAction      The callback to execute when this widget is successfully clicked.
         */
        public Available(int x, int y, EnchantRenderState state, int texIndex, boolean usePlainBackground, Runnable onPressAction) {
            super(x, y, state, texIndex, usePlainBackground, onPressAction);
        }
        // endregion

        // region Logic
        /**
         * Evaluates if the current player meets the resource requirements (Experience and Lapis)
         * to execute this enchantment action. Players in Creative mode automatically bypass these costs.
         *
         * @return true if the player can afford the enchantment, false otherwise.
         */
        private boolean isAffordable() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) return false;
            if (minecraft.player.isCreative()) return true;
            if (!(minecraft.player.containerMenu instanceof EnchantmentMenu menu)) return false;

            int lapisCount = menu.getSlot(1).getItem().getCount();
            int cost = this.state.xpCost();
            int requirement = this.state.xpRequirement();

            return lapisCount >= cost && minecraft.player.experienceLevel >= requirement && minecraft.player.experienceLevel >= cost;
        }
        // endregion

        // region Rendering & Input
        /**
         * Handles mouse click events on this widget.
         * Intercepts the click and denies it if the player cannot afford the enchantment.
         *
         * @param mouseX The X coordinate of the mouse pointer.
         * @param mouseY The Y coordinate of the mouse pointer.
         * @param button The mouse button being pressed.
         * @return true if the click was consumed and processed, false otherwise.
         */
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.isAffordable()) return false;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        /**
         * Renders the widget's background, cost sprites, and text components to the screen.
         * Dynamically adjusts colors and textures based on hover state and affordability.
         *
         * @param context     The GUI graphics context to draw to.
         * @param mouseX      The current mouse X coordinate.
         * @param mouseY      The current mouse Y coordinate.
         * @param partialTick The partial tick time for animations.
         */
        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
            if (this.state.enchantment().isEmpty()) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            EnchantmentSource source = this.state.visualSource();
            int costIndex = Math.max(0, this.state.xpCost() - 1);
            int textX = this.x + this.layout.textXOffset();
            int textY = this.y + this.layout.textYOffset();
            String powerString = String.valueOf(this.state.xpRequirement());

            boolean affordable = this.isAffordable();
            int targetTextColor;
            int powerColor;
            ResourceLocation textureToDraw;
            ResourceLocation levelSprite;

            // Apply disabled aesthetics dynamically if the requirements are not met
            if (!affordable) {
                targetTextColor = source.disabledColor;
                powerColor = ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR;
                textureToDraw = this.usePlainBackground ? source.disabledTexture : source.getDisabledTexture(this.texIndex);
                levelSprite = DISABLED_LEVEL_SPRITES[costIndex];
            } else {
                targetTextColor = this.isHovered ? source.highlightedColor : source.enabledColor;
                powerColor = ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR;
                textureToDraw = this.isHovered
                        ? (this.usePlainBackground ? source.highlightedTexture : source.getHighlightedTexture(this.texIndex))
                        : (this.usePlainBackground ? source.enabledTexture : source.getEnabledTexture(this.texIndex));
                levelSprite = ENABLED_LEVEL_SPRITES[costIndex];
            }

            RenderSystem.enableBlend();
            context.blit(textureToDraw, this.x, this.y, 0, 0, this.layout.buttonWidth(), this.layout.buttonHeight(), this.layout.buttonWidth(), this.layout.buttonHeight());
            context.blitSprite(levelSprite, this.x + this.layout.costXOffset(), this.y + this.layout.costYOffset(), this.layout.costWidth(), this.layout.costHeight());
            RenderSystem.disableBlend();

            MutableComponent displayComponent = this.getFormattedEnchantmentName(source);

            if (!affordable) {
                Style disabledStyle = Style.EMPTY.withColor(targetTextColor);
                displayComponent.withStyle(disabledStyle);
                for (Component sibling : displayComponent.getSiblings()) {
                    if (sibling instanceof MutableComponent mutableSibling) {
                        mutableSibling.withStyle(disabledStyle);
                    }
                }
            }

            int maxTextX = textX + this.layout.textMaxWidth();
            this.drawScrollingString(context, font, displayComponent, textX, textY, maxTextX, targetTextColor);

            int powerTextX = textX + this.layout.powerXOffset() - font.width(powerString);
            int powerTextY = this.y + this.layout.powerYOffset();

            context.drawString(font, powerString, powerTextX + 1, powerTextY, 0, false);
            context.drawString(font, powerString, powerTextX - 1, powerTextY, 0, false);
            context.drawString(font, powerString, powerTextX, powerTextY + 1, 0, false);
            context.drawString(font, powerString, powerTextX, powerTextY - 1, 0, false);
            context.drawString(font, powerString, powerTextX, powerTextY, powerColor, false);
            context.drawString(font, powerString, powerTextX, powerTextY, powerColor, true);
        }
        // endregion
    }
    // endregion

    // region Locked
    /**
     * Represents a widget where the underlying enchantment action cannot be performed
     * due to missing context criteria (e.g., attempting to upgrade an item but providing
     * a source catalyst with an insufficient level or wrong type).
     * <p>
     * Renders identically to an unaffordable available slot, drawing a disabled background
     * and grayed-out text components, and intercepts all mouse interactions to prevent execution.
     */
    public static final class Locked extends EnchantWidget {

        // region Constants
        private static final int ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR = 0x408000;
        // endregion

        // region Initialization
        /**
         * Constructs a new EnchantWidget.Locked.
         *
         * @param x                  The X screen coordinate.
         * @param y                  The Y screen coordinate.
         * @param state              The visual and data state representing this blocked enchantment.
         * @param texIndex           The deterministic texture index (0-9).
         * @param usePlainBackground True if plain backgrounds are enabled.
         * @param onPressAction      The action callback (never invoked due to overridden click logic).
         */
        public Locked(int x, int y, EnchantRenderState state, int texIndex, boolean usePlainBackground, Runnable onPressAction) {
            super(x, y, state, texIndex, usePlainBackground, onPressAction);
        }
        // endregion

        // region Rendering & Input
        /**
         * Denies all interaction with this widget, as the underlying action is locked.
         *
         * @param mouseX The X coordinate of the mouse pointer.
         * @param mouseY The Y coordinate of the mouse pointer.
         * @param button The mouse button being pressed.
         * @return Always false.
         */
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        /**
         * Renders the widget in a disabled visual state.
         *
         * @param context     The GUI graphics context to draw to.
         * @param mouseX      The current mouse X coordinate.
         * @param mouseY      The current mouse Y coordinate.
         * @param partialTick The partial tick time for animations.
         */
        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
            EnchantmentSource source = this.state.visualSource();
            ResourceLocation disabledTex = this.usePlainBackground ? source.disabledTexture : source.getDisabledTexture(this.texIndex);
            RenderSystem.enableBlend();
            context.blit(disabledTex, this.x, this.y, 0, 0, this.layout.buttonWidth(), this.layout.buttonHeight(), this.layout.buttonWidth(), this.layout.buttonHeight());

            if (this.state.enchantment().isEmpty()) {
                RenderSystem.disableBlend();
                return;
            }

            int costIndex = Math.max(0, this.state.xpCost() - 1);
            context.blitSprite(DISABLED_LEVEL_SPRITES[costIndex], this.x + this.layout.costXOffset(), this.y + this.layout.costYOffset(), this.layout.costWidth(), this.layout.costHeight());
            RenderSystem.disableBlend();

            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            int textX = this.x + this.layout.textXOffset();
            int textY = this.y + this.layout.textYOffset();
            String powerString = String.valueOf(this.state.xpRequirement());

            MutableComponent displayComponent = this.getFormattedEnchantmentName(source);
            Style disabledStyle = Style.EMPTY.withColor(source.disabledColor);
            displayComponent.withStyle(disabledStyle);
            for (Component sibling : displayComponent.getSiblings()) {
                if (sibling instanceof MutableComponent mutableSibling) {
                    mutableSibling.withStyle(disabledStyle);
                }
            }

            int maxTextX = textX + this.layout.textMaxWidth();
            this.drawScrollingString(context, font, displayComponent, textX, textY, maxTextX, source.disabledColor);

            int powerTextX = textX + this.layout.powerXOffset() - font.width(powerString);
            int powerTextY = this.y + this.layout.powerYOffset();

            context.drawString(font, powerString, powerTextX + 1, powerTextY, 0, false);
            context.drawString(font, powerString, powerTextX - 1, powerTextY, 0, false);
            context.drawString(font, powerString, powerTextX, powerTextY + 1, 0, false);
            context.drawString(font, powerString, powerTextX, powerTextY - 1, 0, false);
            context.drawString(font, powerString, powerTextX, powerTextY, ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR, false);
            context.drawString(font, powerString, powerTextX, powerTextY, ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR, true);
        }
        // endregion
    }
    // endregion

    // region Maxed
    /**
     * Represents a non-interactable enchantment slot where the target item's enchantment
     * has reached its natural maximum level.
     * <p>
     * This widget provides visual feedback to the player that no further standard upgrades
     * are possible for this specific enchantment, utilizing a distinct texture and text color.
     */
    public static final class Maxed extends EnchantWidget {

        // region Constants
        private static final ResourceLocation ENCHANTMENT_SLOT_MAXED_OUT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/maxed_out.png");
        private static final int ENCHANTMENT_MAXED_OUT_TEXT_COLOR = 0xF2F09D;
        // endregion

        // region Initialization
        /**
         * Constructs a new EnchantWidget.Maxed.
         *
         * @param x                  The initial X screen coordinate.
         * @param y                  The initial Y screen coordinate.
         * @param state              The data state containing the enchantment information.
         * @param texIndex           The deterministic texture index (unused for maxed widgets).
         * @param usePlainBackground Whether plain textures are enabled (unused for maxed widgets).
         * @param onPressAction      The callback invoked on click (never called).
         */
        public Maxed(int x, int y, EnchantRenderState state, int texIndex, boolean usePlainBackground, Runnable onPressAction) {
            super(x, y, state, texIndex, usePlainBackground, onPressAction);
        }
        // endregion

        // region Rendering & Input
        /**
         * Denies all interaction with this widget, as maxed enchantments cannot be upgraded.
         *
         * @param mouseX The X coordinate of the mouse pointer.
         * @param mouseY The Y coordinate of the mouse pointer.
         * @param button The mouse button being pressed.
         * @return Always false.
         */
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        /**
         * Renders the distinct maxed-out background texture and text.
         *
         * @param context     The GUI graphics context.
         * @param mouseX      The current mouse X coordinate.
         * @param mouseY      The current mouse Y coordinate.
         * @param partialTick The partial tick time for animations.
         */
        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
            if (this.state.enchantment().isEmpty()) {
                return;
            }
            context.blit(ENCHANTMENT_SLOT_MAXED_OUT_TEXTURE, this.x, this.y, 0, 0, this.layout.buttonWidth(), this.layout.buttonHeight(), this.layout.buttonWidth(), this.layout.buttonHeight());

            MutableComponent displayComponent = this.getFormattedEnchantmentName(EnchantmentSource.NONE);
            Style maxedStyle = Style.EMPTY.withColor(ENCHANTMENT_MAXED_OUT_TEXT_COLOR);
            displayComponent.withStyle(maxedStyle);
            for (Component sibling : displayComponent.getSiblings()) {
                if (sibling instanceof MutableComponent mutableSibling) {
                    mutableSibling.withStyle(maxedStyle);
                }
            }

            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            int textX = this.x + 8;
            int textY = this.y + this.layout.textYOffset();

            int maxTextX = this.x + this.layout.buttonWidth() - 8;
            this.drawScrollingString(context, font, displayComponent, textX, textY, maxTextX, ENCHANTMENT_MAXED_OUT_TEXT_COLOR);
        }
        // endregion
    }
    // endregion

    // region Overleveled
    /**
     * Represents a non-interactable enchantment slot where the target item's enchantment
     * exceeds its natural maximum level (e.g., pushed beyond the limit via an Enchanted Tome).
     * <p>
     * Displays a unique visual style to indicate its special overleveled status.
     */
    public static final class Overleveled extends EnchantWidget {

        // region Constants
        private static final ResourceLocation ENCHANTMENT_SLOT_OVER_ENCHANTED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/over_enchanted.png");
        private static final int ENCHANTMENT_OVER_ENCHANTED_TEXT_COLOR = 0x4D2299;
        // endregion

        // region Initialization
        /**
         * Constructs a new EnchantWidget.Overleveled.
         *
         * @param x                  The initial X screen coordinate.
         * @param y                  The initial Y screen coordinate.
         * @param state              The data state containing the overleveled enchantment information.
         * @param texIndex           The deterministic texture index.
         * @param usePlainBackground Whether plain textures are enabled.
         * @param onPressAction      The callback invoked on click (never called).
         */
        public Overleveled(int x, int y, EnchantRenderState state, int texIndex, boolean usePlainBackground, Runnable onPressAction) {
            super(x, y, state, texIndex, usePlainBackground, onPressAction);
        }
        // endregion

        // region Rendering & Input
        /**
         * Denies all interaction, as overleveled enchantments cannot be further upgraded directly.
         *
         * @param mouseX The X coordinate of the mouse pointer.
         * @param mouseY The Y coordinate of the mouse pointer.
         * @param button The mouse button being pressed.
         * @return Always false.
         */
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        /**
         * Renders the distinct overleveled background texture and deep purple text.
         *
         * @param context     The GUI graphics context.
         * @param mouseX      The current mouse X coordinate.
         * @param mouseY      The current mouse Y coordinate.
         * @param partialTick The partial tick time.
         */
        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
            if (this.state.enchantment().isEmpty()) {
                return;
            }
            context.blit(ENCHANTMENT_SLOT_OVER_ENCHANTED_TEXTURE, this.x, this.y, 0, 0, this.layout.buttonWidth(), this.layout.buttonHeight(), this.layout.buttonWidth(), this.layout.buttonHeight());

            MutableComponent displayComponent = this.getFormattedEnchantmentName(EnchantmentSource.NONE);
            Style overleveledStyle = Style.EMPTY.withColor(ENCHANTMENT_OVER_ENCHANTED_TEXT_COLOR);
            displayComponent.withStyle(overleveledStyle);
            for (Component sibling : displayComponent.getSiblings()) {
                if (sibling instanceof MutableComponent mutableSibling) {
                    mutableSibling.withStyle(overleveledStyle);
                }
            }

            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            int textX = this.x + 8;
            int textY = this.y + this.layout.textYOffset();

            int maxTextX = this.x + this.layout.buttonWidth() - 8;
            this.drawScrollingString(context, font, displayComponent, textX, textY, maxTextX, ENCHANTMENT_OVER_ENCHANTED_TEXT_COLOR);
        }
        // endregion
    }
    // endregion
}