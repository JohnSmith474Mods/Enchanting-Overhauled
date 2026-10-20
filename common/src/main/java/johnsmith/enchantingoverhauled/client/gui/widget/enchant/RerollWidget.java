package johnsmith.enchantingoverhauled.client.gui.widget.enchant;

import com.mojang.blaze3d.systems.RenderSystem;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipe;
import johnsmith.enchantingoverhauled.client.gui.screen.enchant.MenuLayoutConfig;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.menu.enchant.EnchantmentMenu;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the rendering, tooltip generation, and interaction logic for the "Reroll" button
 * within the Enchantment Screen.
 * <p>
 * Re-rolling consumes resources based on the item's existing enchantments to refresh the
 * generated table offerings without needing to physically remove and replace the item.
 */
public class RerollWidget {

    // region Constants
    private static final ResourceLocation REROLL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/reroll.png");
    private static final ResourceLocation REROLL_HIGHLIGHTED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/reroll.png");
    private static final ResourceLocation REROLL_DISABLED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/reroll.png");

    private static final ResourceLocation[] ENABLED_LEVEL_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3")};
    private static final ResourceLocation[] DISABLED_LEVEL_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1_disabled"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2_disabled"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3_disabled")};
    // endregion

    // region Fields
    private final EnchantmentMenu menu;
    private final MenuLayoutConfig layout;
    private final Runnable onPress;
    private int x, y;
    private boolean isHovered;
    // endregion

    // region Initialization
    /**
     * Constructs a new RerollWidget.
     *
     * @param menu    The active EnchantmentMenu providing container state.
     * @param layout  The screen layout configuration for drawing boundaries.
     * @param onPress The callback fired when the button is successfully clicked.
     */
    public RerollWidget(EnchantmentMenu menu, MenuLayoutConfig layout, Runnable onPress) {
        this.menu = menu;
        this.layout = layout;
        this.onPress = onPress;
    }
    // endregion

    // region Rendering & Input
    /**
     * Updates the alignment origin for the widget.
     *
     * @param alignX The parent screen's left position.
     * @param alignY The parent screen's top position.
     */
    public void updatePosition(int alignX, int alignY) {
        this.x = alignX + layout.rerollButtonXOffset();
        this.y = alignY + layout.rerollButtonYOffset();
    }

    /**
     * Renders the button background and cost sprite based on affordability and hover states.
     *
     * @param context     The GUI graphics context.
     * @param mouseX      The current mouse X coordinate.
     * @param mouseY      The current mouse Y coordinate.
     * @param partialTick The partial tick time.
     * @param lapisCount  The available lapis lazuli in the container.
     * @param minecraft   The Minecraft client instance.
     */
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick, int lapisCount, Minecraft minecraft) {
        this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + layout.rerollButtonWidth() && mouseY < y + layout.rerollButtonHeight();
        ItemStack target = this.menu.getEnchantmentTarget();
        boolean targetIsEmpty = target.isEmpty();

        boolean hasRecipe = EnchantRecipe.get(minecraft.level.registryAccess(), target).isPresent();
        boolean targetIsEnchantable = !targetIsEmpty && (hasRecipe || target.isEnchantable());

        boolean targetIsSourceEnchantable = this.menu.getActiveContexts().stream().anyMatch(ctx -> ctx.pending().source() == EnchantmentSource.SOURCE);
        boolean hasTableSource = this.menu.getActiveContexts().stream().anyMatch(ctx -> ctx.pending().source() == EnchantmentSource.TABLE);

        ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
        int occupiedSlots = EnchantmentLib.getEnchantments(curseFreeTarget).size();

        int rerollCost = occupiedSlots + 1;
        int costIndex = Math.max(0, rerollCost - 1);
        boolean canReroll = occupiedSlots < Config.MAX_ENCHANTMENT_AMOUNT.get() && !targetIsSourceEnchantable && targetIsEnchantable && hasTableSource;

        RenderSystem.enableBlend();

        boolean isCreative = minecraft.player != null && minecraft.player.isCreative();
        int playerXp = minecraft.player != null ? minecraft.player.experienceLevel : 0;
        boolean cannotAfford = (lapisCount < rerollCost || playerXp < rerollCost) && !isCreative;

        boolean rerollEnabled = !targetIsEmpty && targetIsEnchantable && canReroll && !cannotAfford;

        if (rerollEnabled) {
            if (this.isHovered) {
                context.blit(REROLL_HIGHLIGHTED_TEXTURE, x, y, 0, 0, layout.rerollButtonWidth(), layout.rerollButtonHeight(), layout.rerollButtonWidth(), layout.rerollButtonHeight());
            } else {
                context.blit(REROLL_TEXTURE, x, y, 0, 0, layout.rerollButtonWidth(), layout.rerollButtonHeight(), layout.rerollButtonWidth(), layout.rerollButtonHeight());
            }
            if (costIndex < ENABLED_LEVEL_SPRITES.length) {
                context.blitSprite(ENABLED_LEVEL_SPRITES[costIndex], x + layout.rerollCostXOffset(), y + layout.rerollCostYOffset(), layout.rerollCostWidth(), layout.rerollCostHeight());
            }
        } else {
            context.blit(REROLL_DISABLED_TEXTURE, x, y, 0, 0, layout.rerollButtonWidth(), layout.rerollButtonHeight(), layout.rerollButtonWidth(), layout.rerollButtonHeight());
            boolean showDisabledCost = !targetIsEmpty && targetIsEnchantable && canReroll;
            if (showDisabledCost && costIndex < DISABLED_LEVEL_SPRITES.length) {
                context.blitSprite(DISABLED_LEVEL_SPRITES[costIndex], x + layout.rerollCostXOffset(), y + layout.rerollCostYOffset(), layout.rerollCostWidth(), layout.rerollCostHeight());
            }
        }
        RenderSystem.disableBlend();
    }

    /**
     * Evaluates and renders the tooltip describing the reroll action and its resource costs.
     *
     * @param context    The GUI graphics context.
     * @param mouseX     The mouse X coordinate.
     * @param mouseY     The mouse Y coordinate.
     * @param font       The client font renderer.
     * @param lapisCount The available lapis lazuli in the container.
     * @param minecraft  The Minecraft client instance.
     * @return true if the tooltip was drawn, false otherwise.
     */
    public boolean renderTooltip(GuiGraphics context, int mouseX, int mouseY, Font font, int lapisCount, Minecraft minecraft) {
        if (this.isHovered) {
            boolean isCreative = minecraft.player != null && minecraft.player.isCreative();
            ItemStack target = this.menu.getEnchantmentTarget();
            boolean targetIsEmpty = target.isEmpty();

            boolean hasRecipe = EnchantRecipe.get(minecraft.level.registryAccess(), target).isPresent();
            boolean targetIsEnchantable = !targetIsEmpty && (hasRecipe || target.isEnchantable());

            boolean targetIsSourceEnchantable = this.menu.getActiveContexts().stream().anyMatch(ctx -> ctx.pending().source() == EnchantmentSource.SOURCE);
            boolean hasTableSource = this.menu.getActiveContexts().stream().anyMatch(ctx -> ctx.pending().source() == EnchantmentSource.TABLE);

            ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
            int occupiedSlots = EnchantmentLib.getEnchantments(curseFreeTarget).size();

            int rerollCost = occupiedSlots + 1;
            boolean canReroll = occupiedSlots < Config.MAX_ENCHANTMENT_AMOUNT.get() && !targetIsSourceEnchantable && targetIsEnchantable && hasTableSource;

            List<FormattedCharSequence> list = new ArrayList<>();
            list.add(Component.translatable("gui.enchanting_overhauled.turn_page").withStyle(ChatFormatting.WHITE).getVisualOrderText());

            if (!targetIsEmpty && canReroll) {
                if (!isCreative) {
                    list.add(FormattedCharSequence.EMPTY);
                    int playerXp = minecraft.player != null ? minecraft.player.experienceLevel : 0;

                    if (playerXp < rerollCost) {
                        list.add(Component.translatable("container.enchant.level.requirement", rerollCost).withStyle(ChatFormatting.RED).getVisualOrderText());
                    } else {
                        MutableComponent mutableText = (rerollCost == 1) ? Component.translatable("container.enchant.lapis.one") : Component.translatable("container.enchant.lapis.many", rerollCost);
                        list.add(mutableText.withStyle(lapisCount >= rerollCost ? ChatFormatting.GRAY : ChatFormatting.RED).getVisualOrderText());

                        MutableComponent mutableText2 = (rerollCost == 1) ? Component.translatable("container.enchant.level.one") : Component.translatable("container.enchant.level.many", rerollCost);
                        list.add(mutableText2.withStyle(ChatFormatting.GRAY).getVisualOrderText());
                    }
                }
            }
            context.renderTooltip(font, list, mouseX, mouseY);
            return true;
        }
        return false;
    }

    /**
     * Executes the reroll logic if the button is hovered and clicked with the primary mouse button.
     *
     * @param mouseX The mouse X coordinate.
     * @param mouseY The mouse Y coordinate.
     * @param button The mouse button pressed.
     * @return true if the click was consumed, false otherwise.
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered && button == 0) {
            this.onPress.run();
            return true;
        }
        return false;
    }
    // endregion
}