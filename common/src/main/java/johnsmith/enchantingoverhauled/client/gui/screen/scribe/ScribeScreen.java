package johnsmith.enchantingoverhauled.client.gui.screen.scribe;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.client.gui.tooltip.scribe.ScribeTooltipContext;
import johnsmith.enchantingoverhauled.client.gui.tooltip.scribe.ScribeTooltipProvider;
import johnsmith.enchantingoverhauled.client.gui.widget.scribe.ScribeActionWidget;
import johnsmith.enchantingoverhauled.client.gui.widget.scribe.ScribeGridWidget;
import johnsmith.enchantingoverhauled.client.gui.widget.scribe.ScribeHazardWidget;
import johnsmith.enchantingoverhauled.client.gui.widget.scribe.ScribePageWidget;
import johnsmith.enchantingoverhauled.client.gui.widget.scribe.ScribeWidgetFactory;
import johnsmith.enchantingoverhauled.menu.scribe.ScribeMenu;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGameState;
import johnsmith.enchantingoverhauled.menu.scribe.controller.ScribeInput;
import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.client.gui.sound.SoundEffectData;
import johnsmith.enchantingoverhauled.client.gui.sound.SoundEffectDataFactory;
import johnsmith.enchantingoverhauled.client.gui.sound.scribe.ScribeSoundEffectSpecification;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.NotNull;

/**
 * The Client UI representing the Scribing Minigame.
 */
public class ScribeScreen extends AbstractContainerScreen<ScribeMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/background.png");
    private static final ResourceLocation BOOK_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/book_background.png");
    private static final ResourceLocation TOME_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/tome_background.png");
    private static final ResourceLocation EMPTY_SLOT_SOURCE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/empty_slot_enchanted_page.png");
    private static final ResourceLocation EMPTY_SLOT_FUEL_TEXTURE = ResourceLocation.withDefaultNamespace("textures/item/empty_slot_lapis_lazuli.png");

    private ScribeGridWidget gridWidget;
    private ScribeActionWidget actionWidget;
    private ScribePageWidget sourcePageWidget;
    private ScribePageWidget indexPageWidget;
    private ScribeHazardWidget hazardWidget;

    private int lossTickTimer = -1;
    private ScribeGameState lastKnownState = ScribeGameState.MISSING_SOURCE;

    public ScribeScreen(ScribeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 228;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        this.gridWidget = new ScribeGridWidget(this.menu.getGridData());
        this.sourcePageWidget = new ScribePageWidget(true, this.menu);
        this.indexPageWidget = new ScribePageWidget(false, this.menu);
        this.hazardWidget = new ScribeHazardWidget();

        this.actionWidget = ScribeWidgetFactory.create(
                this.menu.getGridData(),
                () -> this.menu.getSlot(1).getItem().getCount(),
                () -> this.minecraft != null && this.minecraft.player != null && this.minecraft.player.isCreative()
        );
        this.actionWidget.updatePosition(this.leftPos, this.topPos);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        this.actionWidget = ScribeWidgetFactory.create(
                this.menu.getGridData(),
                () -> this.menu.getSlot(1).getItem().getCount(),
                () -> this.minecraft != null && this.minecraft.player != null && this.minecraft.player.isCreative()
        );
        this.actionWidget.updatePosition(this.leftPos, this.topPos);

        ScribeGameState currentState = this.menu.getGridData().getGameState();
        if (currentState != this.lastKnownState) {
            // Play state transition sounds
            if (currentState == ScribeGameState.WON) {
                ScribeSoundEffectSpecification.DEFAULT.gameWon().play();
            } else if (currentState == ScribeGameState.LOST) {
                ScribeSoundEffectSpecification.DEFAULT.gameLost().play();
            }
            this.lastKnownState = currentState;
        }

        if (currentState == ScribeGameState.LOST) {
            if (this.lossTickTimer == -1) {
                this.lossTickTimer = Config.SCRIBE_FAILURE_DISPLAY_TIME.get() * 20;
            } else if (this.lossTickTimer > 0) {
                this.lossTickTimer--;
            } else if (this.lossTickTimer == 0) {
                if (this.minecraft != null && this.minecraft.gameMode != null) {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, new ScribeInput(0, 0, ScribeInput.ACTION_ACK_LOSS).encode());
                }
                this.lossTickTimer = -1;
            }
        } else {
            this.lossTickTimer = -1;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ScribeGameState state = this.menu.getGridData().getGameState();
        int payload = -1;

        // Index Interaction (Enchantment Selection)
        if (state.ordinal() < ScribeGameState.PENDING.ordinal()) {
            payload = this.sourcePageWidget.mouseClicked(mouseX, mouseY);
        }

        if (payload == -1) {
            payload = this.actionWidget.mouseClicked(mouseX, mouseY);
        }

        // Minigame Grid Input
        if (payload == -1 && state.ordinal() >= ScribeGameState.PENDING.ordinal()) {
            payload = this.gridWidget.mouseClicked(mouseX, mouseY, button);
        }

        if (payload != -1) {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                ScribeInput input = ScribeInput.decode(payload);

                // Map interactions to specific sound effects using the specification
                SoundEffectData sound = switch (input.action()) {
                    case ScribeInput.ACTION_REVEAL -> ScribeSoundEffectSpecification.DEFAULT.gridReveal();
                    case ScribeInput.ACTION_FLAG -> {
                        // Because the UI evaluates clicks prior to state sync, we read the exact flag status
                        // as it was the moment the user clicked.
                        if (this.menu.getGridData().isFlagged(input.x(), input.y())) {
                            yield ScribeSoundEffectSpecification.DEFAULT.gridUnflag();
                        } else {
                            yield ScribeSoundEffectSpecification.DEFAULT.gridFlag();
                        }
                    }
                    case ScribeInput.ACTION_START -> ScribeSoundEffectSpecification.DEFAULT.actionStart();
                    case ScribeInput.ACTION_ABORT -> ScribeSoundEffectSpecification.DEFAULT.actionAbort();
                    case ScribeInput.ACTION_TAKE_BOOK -> ScribeSoundEffectSpecification.DEFAULT.actionIdle();
                    case ScribeInput.ACTION_RETURN_TO_INDEX -> ScribeSoundEffectSpecification.DEFAULT.actionWon();
                    case ScribeInput.ACTION_SELECT_ENCHANTMENT -> SoundEffectDataFactory.create(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    default -> ScribeSoundEffectSpecification.DEFAULT.actionLost();
                };

                sound.play();
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, payload);
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.sourcePageWidget.mouseScrolled(mouseX, mouseY, verticalAmount)) {
            return true;
        }
        if (this.indexPageWidget.mouseScrolled(mouseX, mouseY, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        ScribeGameState state = this.menu.getGridData().getGameState();

        if (state == ScribeGameState.LOST || state == ScribeGameState.WON) {
            Component msg = state == ScribeGameState.WON ? Component.translatable("gui.enchanting_overhauled.scribe.success").withColor(0x00FF00) : Component.translatable("gui.enchanting_overhauled.scribe.failed").withColor(0xFF0000);
            int textWidth = this.font.width(msg);
            guiGraphics.drawString(this.font, msg, this.leftPos + (this.imageWidth / 2) - (textWidth / 2), this.topPos + 5, 0xFFFFFF, true);
        }

        // Render hovered enchantment tooltip
        ScribeTooltipContext hovered = null;
        if (state.ordinal() < ScribeGameState.PENDING.ordinal()) {
            hovered = this.sourcePageWidget.getHoveredTooltip();
            if (hovered == null) {
                hovered = this.indexPageWidget.getHoveredTooltip();
            }
        }

        if (hovered != null) {
            ScribeTooltipProvider.renderTooltip(guiGraphics, this.font, hovered, mouseX, mouseY);
        }

        // Render Action Button Tooltip
        if (this.actionWidget != null) {
            this.actionWidget.renderTooltip(guiGraphics, mouseX, mouseY, this.font);
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        ResourceLocation innerBg = this.menu.getGridData().isTome() ? TOME_BACKGROUND : BOOK_BACKGROUND;
        guiGraphics.blit(innerBg, this.leftPos + 4, this.topPos + 17, 0, 0, 165, 85, 165, 85);

        ScribeGameState state = this.menu.getGridData().getGameState();

        if (state.ordinal() < ScribeGameState.PENDING.ordinal()) {
            // Shift lists down by 2px (from the old 36 to 38) to properly pad the title
            this.sourcePageWidget.updatePosition(this.leftPos + 14, this.topPos + 36);
            this.sourcePageWidget.render(guiGraphics, mouseX, mouseY, this.font);

            this.indexPageWidget.updatePosition(this.leftPos + 91, this.topPos + 36);
            this.indexPageWidget.render(guiGraphics, mouseX, mouseY, this.font);
        } else {
            this.hazardWidget.render(guiGraphics, this.font, this.leftPos, this.topPos, this.menu.containerId);

            this.gridWidget.updatePosition(this.leftPos, this.topPos);
            this.gridWidget.render(guiGraphics, mouseX, mouseY, this.font); // Added font dependency
        }

        if (!this.menu.getSlot(0).hasItem()) {
            guiGraphics.blit(EMPTY_SLOT_SOURCE_TEXTURE, this.leftPos + 8, this.topPos + 108, 0, 0, 16, 16, 16, 16);
        }
        if (!this.menu.getSlot(1).hasItem()) {
            guiGraphics.blit(EMPTY_SLOT_FUEL_TEXTURE, this.leftPos + 26, this.topPos + 108, 0, 0, 16, 16, 16, 16);
        }

        if (this.actionWidget != null) {
            this.actionWidget.render(guiGraphics, mouseX, mouseY, this.font);
        }

        if (state == ScribeGameState.LOST && this.lossTickTimer > 0) {
            float progress = (float) this.lossTickTimer / (Config.SCRIBE_FAILURE_DISPLAY_TIME.get() * 20);
            int barWidth = (int) (161 * progress);
            int barColor = Config.SCRIBE_FAILURE_PROGRESS_COLOR.get() | 0xFF000000;
            guiGraphics.fill(this.leftPos + 7, this.topPos + 15, this.leftPos + 7 + barWidth, this.topPos + 17, barColor);
        }
    }
}