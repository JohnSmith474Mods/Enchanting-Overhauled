
package johnsmith.enchantingoverhauled.enchantment;

import com.mojang.blaze3d.systems.RenderSystem;
import johnsmith.enchantingoverhauled.Common;
import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OverhauledEnchantmentScreen extends AbstractContainerScreen<OverhauledEnchantmentMenu> {
    /** Should match the visible (clickable) entries in the GUI */
    private static final int MAX_SCROLL = 3;
    private static final String PREFIX = "textures/gui/container/enchanting_table/button/";

    private int currentScroll;

    public OverhauledEnchantmentScreen(final OverhauledEnchantmentMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title.copy().withStyle(title.getStyle().withFont(GALACTIC_FONT_ID).withColor(ChatFormatting.YELLOW)));
        this.imageHeight += SCREEN_HEIGHT_ADJUSTMENT;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        int clickedButton = Integer.MIN_VALUE;

        for (int slot = currentScroll; slot < OverhauledEnchantmentMenu.AVAILABLE_SLOTS; slot++) {
            double mouseDiffX = mouseX - (double) (leftPos + ENCHANTING_BUTTON_X_OFFSET);
            double mouseDiffY = mouseY - (double) (topPos + ENCHANTING_BUTTON_Y_OFFSET + ENCHANTING_BUTTON_HEIGHT * (slot - currentScroll));

            if (mouseDiffX >= 0 && mouseDiffY >= 0 && mouseDiffX < ENCHANTING_BUTTON_WIDTH && mouseDiffY < ENCHANTING_BUTTON_HEIGHT) {
                clickedButton = slot;
                break;
            }
        }

        if (clickedButton == Integer.MIN_VALUE) {
            double mouseDiffX = mouseX - (double) (leftPos + REROLL_BUTTON_X_OFFSET);
            double mouseDiffY = mouseY - (double) (topPos + REROLL_BUTTON_Y_OFFSET);

            if (mouseDiffX >= 0 && mouseDiffY >= 0 && mouseDiffX < REROLL_BUTTON_WIDTH && mouseDiffY < REROLL_BUTTON_HEIGHT) {
                clickedButton = OverhauledEnchantmentMenu.REROLL_BUTTON;
            }
        }

        if (clickedButton != Integer.MIN_VALUE) {
            //noinspection DataFlowIssue -> minecraft is present
            if (menu.clickMenuButton(minecraft.player, clickedButton)) {
                //noinspection DataFlowIssue -> minecraft is present
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, clickedButton);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollX, final double scrollY) {
        int previous = currentScroll;
        int lastFilledSlot = 0;

        for (int slot = 0; slot < menu.enchantmentSources.length; slot++) {
            int source = menu.enchantmentSources[slot];

            if (source == EnchantmentSource.NONE.getId()) {
                break;
            }

            lastFilledSlot = slot;
        }

        int max = Math.min(lastFilledSlot + 1, OverhauledEnchantmentMenu.AVAILABLE_SLOTS) - MAX_SCROLL;
        // Negate the scroll so that scrolling down shows further entries
        currentScroll = Math.clamp(currentScroll + (int) -scrollY, 0, Math.max(0, max));

        return previous != currentScroll;
    }

    @Override
    public void render(@NotNull final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        //noinspection DataFlowIssue -> minecraft and player are present
        var map = minecraft.player.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap();

        for (int slot = currentScroll; slot < OverhauledEnchantmentMenu.AVAILABLE_SLOTS; slot++) {
            Holder<Enchantment> enchantment = map.byId(menu.enchantClue[slot]);
            int powerRequirement = menu.enchantingPower[slot];
            int level = menu.levelClue[slot];

            if (enchantment == null || level <= 0 || powerRequirement <= 0) {
                continue;
            }

            int buttonY = ENCHANTING_BUTTON_Y_OFFSET + (ENCHANTING_BUTTON_HEIGHT * (slot - currentScroll));

            if (isHovering(ENCHANTING_BUTTON_X_OFFSET, buttonY, ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT, mouseX, mouseY)) {
                EnchantmentSource source = EnchantmentSource.byId(menu.enchantmentSources[slot]);

                switch (source) {
                    case TARGET -> {
                        if (level >= enchantment.value().getMaxLevel()) {
                            renderMaxTooltip(graphics, mouseX, mouseY, enchantment, level);
                        } else {
                            List<FormattedCharSequence> tooltip = new ArrayList<>();
                            int cost = menu.calculateEnchantmentCost(enchantment);

                            tooltip.add(Component.translatable("gui.enchanting_overhauled.upgrade").withStyle(ChatFormatting.WHITE).getVisualOrderText());
                            collectEnchantmentTooltip(enchantment, level, tooltip);
                            collectCostTooltip(tooltip, powerRequirement, cost);

                            graphics.renderTooltip(font, tooltip, mouseX, mouseY);
                        }
                    }
                    case SOURCE -> {
                        List<FormattedCharSequence> tooltip = new ArrayList<>();
                        int cost = menu.calculateEnchantmentCost(enchantment);

                        tooltip.add(Component.translatable("gui.enchanting_overhauled.transfer").withStyle(ChatFormatting.WHITE).getVisualOrderText());
                        collectEnchantmentTooltip(enchantment, level, tooltip);
                        collectCostTooltip(tooltip, powerRequirement, cost);

                        graphics.renderTooltip(font, tooltip, mouseX, mouseY);
                    }
                    case TABLE -> {
                        List<FormattedCharSequence> tooltip = new ArrayList<>();
                        int cost = menu.calculateEnchantmentCost(enchantment);

                        tooltip.add(Component.translatable("gui.enchanting_overhauled.apply").withStyle(ChatFormatting.WHITE).getVisualOrderText());

                        ResourceKey<EnchantmentTheme> themeKey = EnchantmentLib.getThemeKey(minecraft.player.registryAccess(), enchantment);
                        Registry<EnchantmentTheme> registry = Services.PLATFORM.getThemeRegistry(minecraft.player.registryAccess()).orElse(null);

                        Component name = Enchantment.getFullname(enchantment, level);

                        int color = 0xFFFFFF;
                        TextColor enchantmentColor = name.getStyle().getColor();

                        if (enchantmentColor != null) {
                            color = enchantmentColor.getValue();
                        }

                        if (registry != null) {
                            EnchantmentTheme theme = registry.get(themeKey);

                            if (theme != null && theme.colorCode().isPresent()) {
                                color = theme.colorCode().get();
                            }
                        }

                        MutableComponent mutableName = name.copy().withStyle(name.getStyle().withColor(color));

                        if (Config.BINARY_ACCESSIBILITY_OBFUSCATE_NEW_ENCHANTMENTS.get()) {
                            mutableName.withStyle(mutableName.getStyle().withFont(GALACTIC_FONT_ID));
                        }

                        tooltip.add(mutableName.getVisualOrderText());

                        if (Config.BINARY_ACCESSIBILITY_SHOW_ENCHANTMENT_DESCRIPTIONS.get()) {
                            //noinspection OptionalGetWithoutIsPresent -> The registry only returns Holder.Reference
                            String descriptionKey = Util.makeDescriptionId("enchantment", enchantment.unwrapKey().get().location()) + ".desc";

                            if (I18n.exists(descriptionKey)) {
                                MutableComponent description = Component.translatable(descriptionKey).withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR.get());

                                if (Config.BINARY_ACCESSIBILITY_OBFUSCATE_NEW_ENCHANTMENTS.get()) {
                                    description = description.withStyle(description.getStyle().withFont(GALACTIC_FONT_ID));
                                }

                                tooltip.addAll(EnchantmentLib.wrapDescription(description));
                            }
                        }

                        collectCostTooltip(tooltip, powerRequirement, cost);
                        graphics.renderTooltip(font, tooltip, mouseX, mouseY);
                    }
                }

                break;
            } else if (isHovering(REROLL_BUTTON_X_OFFSET, REROLL_BUTTON_Y_OFFSET, REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT, mouseX, mouseY)) {
                List<FormattedCharSequence> tooltip = new ArrayList<>();
                tooltip.add(Component.translatable("gui.enchanting_overhauled.turn_page").withStyle(ChatFormatting.WHITE).getVisualOrderText());

                int rerollCost = getRerollCost();

                if (rerollCost != -1) {
                    collectCostTooltip(tooltip, rerollCost, rerollCost);
                }

                graphics.renderTooltip(font, tooltip, mouseX, mouseY);

                break;
            }
        }
    }

    @Override
    protected void renderBg(@NotNull final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        graphics.blit(BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        //noinspection DataFlowIssue -> minecraft and level are expected to be present
        var map = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
        EnchantmentNames.getInstance().initSeed(menu.getEnchantmentSeed());

        boolean usePlain = Config.BINARY_ACCESSIBILITY_USE_PLAIN_BACKGROUND.get();

        for (int buttonIndex = currentScroll; buttonIndex < OverhauledEnchantmentMenu.AVAILABLE_SLOTS; buttonIndex++) {
            if (buttonIndex - currentScroll == MAX_SCROLL) {
                break;
            }

            int buttonX = leftPos + ENCHANTING_BUTTON_X_OFFSET;
            int buttonY = topPos + ENCHANTING_BUTTON_Y_OFFSET + ENCHANTING_BUTTON_HEIGHT * (buttonIndex - currentScroll);

            int enchantingPower = menu.enchantingPower[buttonIndex];
            int source = menu.enchantmentSources[buttonIndex];
            int enchantmentId = menu.enchantClue[buttonIndex];
            int level = menu.levelClue[buttonIndex];

            Holder<Enchantment> enchantment = map.byId(enchantmentId);

            // Empty slot
            if (enchantingPower <= 0 || enchantment == null) {
                RenderSystem.enableBlend();

                ResourceLocation resource = usePlain
                        ? EnchantmentSource.TARGET.getDisabledTexture() :
                        Common.resource(PREFIX + "disabled/table_" + menu.tableTextureIndices[buttonIndex] + ".png");

                graphics.blit(resource, buttonX, buttonY, 0, 0,
                        ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT,
                        ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT
                );

                RenderSystem.disableBlend();
                continue;
            }

            if (EnchantmentSource.byId(source) == EnchantmentSource.TARGET) {
                if (level >= enchantment.value().getMaxLevel()) {
                    renderFullyUpgradedSlot(graphics, enchantment, level, buttonX, buttonY);
                } else {
                    renderEnchantmentSlot(graphics, mouseX, mouseY, enchantment, buttonIndex, buttonX, buttonY);
                }
            } else {
                renderEnchantmentSlot(graphics, mouseX, mouseY, enchantment, buttonIndex, buttonX, buttonY);
            }
        }

        renderReroll(graphics, mouseX, mouseY);
        renderExperienceBar(graphics);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY + 29, 4210752, false);
    }

    private int getRerollCost() {
        ItemStack target = menu.getSlot(OverhauledEnchantmentMenu.ITEM_TO_ENCHANT_SLOT).getItem();

        if (target.isEmpty() || (!target.isEnchantable() && !target.is(Items.BOOK))) {
            return -1;
        }

        boolean hasTableEnchantments = false;

        for (int enchantmentSource : menu.enchantmentSources) {
            if (enchantmentSource == EnchantmentSource.SOURCE.getId()) {
                return -1;
            } else if (enchantmentSource == EnchantmentSource.TABLE.getId()) {
                hasTableEnchantments = true;
            }
        }

        if (!hasTableEnchantments) {
            return -1;
        }

        int enchantmentAmount = EnchantmentLib.getEnchantments(EnchantmentLib.removeCursesFrom(target)).size();
        return enchantmentAmount + 1;
    }

    private void collectCostTooltip(final List<FormattedCharSequence> tooltip, final int powerRequirement, final int cost) {
        //noinspection DataFlowIssue -> minecraft and player are present
        if (!minecraft.player.hasInfiniteMaterials()) {
            tooltip.add(FormattedCharSequence.EMPTY);

            if (minecraft.player.experienceLevel < powerRequirement) {
                tooltip.add(Component.translatable("container.enchant.level.requirement", powerRequirement).withStyle(ChatFormatting.RED).getVisualOrderText());
            } else {
                MutableComponent lapisCost;

                if (cost == 1) {
                    lapisCost = Component.translatable("container.enchant.lapis.one");
                } else {
                    lapisCost = Component.translatable("container.enchant.lapis.many", cost);
                }

                tooltip.add(lapisCost.withStyle(getLapisAmount() >= cost ? ChatFormatting.GRAY : ChatFormatting.RED).getVisualOrderText());

                MutableComponent experienceLevelCost;

                if (cost == 1) {
                    experienceLevelCost = Component.translatable("container.enchant.level.one");
                } else {
                    experienceLevelCost = Component.translatable("container.enchant.level.many", cost);
                }

                tooltip.add(experienceLevelCost.withStyle(ChatFormatting.GRAY).getVisualOrderText());
            }
        }
    }

    private void renderMaxTooltip(final @NotNull GuiGraphics graphics, final int mouseX, final int mouseY, final Holder<Enchantment> enchantment, final int level) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();
        collectEnchantmentTooltip(enchantment, level, tooltip);
        graphics.renderTooltip(font, tooltip, mouseX, mouseY);
    }

    private static void collectEnchantmentTooltip(final Holder<Enchantment> enchantment, final int level, final List<FormattedCharSequence> tooltip) {
        tooltip.add(Enchantment.getFullname(enchantment, level).getVisualOrderText());

        if (Config.BINARY_ACCESSIBILITY_SHOW_ENCHANTMENT_DESCRIPTIONS.get()) {
            //noinspection OptionalGetWithoutIsPresent -> The registry only returns Holder.Reference
            String descriptionKey = Util.makeDescriptionId("enchantment", enchantment.unwrapKey().get().location()) + ".desc";

            if (I18n.exists(descriptionKey)) {
                MutableComponent description = Component.translatable(descriptionKey).withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR.get());
                tooltip.addAll(EnchantmentLib.wrapDescription(description));
            }
        }
    }

    private void renderExperienceBar(final @NotNull GuiGraphics graphics) {
        int barX = leftPos + EXPERIENCE_BAR_X_OFFSET;
        int barY = topPos + EXPERIENCE_BAR_Y_OFFSET;

        graphics.blit(EXPERIENCE_BAR_INACTIVE, barX, barY, 0, 0,
                EXPERIENCE_BAR_WIDTH, EXPERIENCE_BAR_HEIGHT,
                EXPERIENCE_BAR_WIDTH, EXPERIENCE_BAR_HEIGHT
        );

        //noinspection DataFlowIssue -> minecraft is present
        int width = (int) (minecraft.player.experienceProgress * (float) EXPERIENCE_BAR_WIDTH);

        if (width > 0) {
            graphics.blit(EXPERIENCE_BAR_ACTIVE, barX, barY, 0, 0,
                    width, EXPERIENCE_BAR_HEIGHT,
                    EXPERIENCE_BAR_WIDTH, EXPERIENCE_BAR_HEIGHT
            );
        }

        if (minecraft.player.experienceLevel == 0) {
            return;
        }

        String level = String.valueOf(minecraft.player.experienceLevel);

        int textX = barX + (EXPERIENCE_BAR_WIDTH / 2) - (font.width(level) / 2);
        int textY = topPos + EXPERIENCE_BAR_Y_OFFSET - 6;

        // Black outline
        graphics.drawString(font, level, textX + 1, textY, BLACK, false);
        graphics.drawString(font, level, textX - 1, textY, BLACK, false);
        graphics.drawString(font, level, textX, textY + 1, BLACK, false);
        graphics.drawString(font, level, textX, textY - 1, BLACK, false);

        graphics.drawString(font, level, textX, textY, ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR, false);
    }

    private void renderReroll(final @NotNull GuiGraphics graphics, final int mouseX, final int mouseY) {
        ItemStack target = menu.getSlot(OverhauledEnchantmentMenu.ITEM_TO_ENCHANT_SLOT).getItem();
        ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);

        boolean isValidItem = !target.isEmpty() && (target.isEnchantable() || target.is(Items.BOOK));

        boolean hasTableSource = false;
        boolean isSourceEnchantable = false;

        for (int enchantmentSource : menu.enchantmentSources) {
            if (enchantmentSource == EnchantmentSource.TABLE.getId()) {
                hasTableSource = true;
            }

            if (enchantmentSource == EnchantmentSource.SOURCE.getId()) {
                isSourceEnchantable = true;
            }

            if (hasTableSource && isSourceEnchantable) {
                break;
            }
        }

        boolean canReroll = isValidItem && hasTableSource && !isSourceEnchantable;

        int enchantmentCount = EnchantmentLib.getEnchantments(curseFreeTarget).size();
        int rerollCost = enchantmentCount + 1;
        int costIndex = Math.clamp(enchantmentCount, 0, 2);

        //noinspection DataFlowIssue -> player is present
        boolean canAfford = minecraft.player.hasInfiniteMaterials()
                || (getLapisAmount() >= rerollCost && minecraft.player.experienceLevel >= rerollCost);

        RenderSystem.enableBlend();

        int x = leftPos + REROLL_BUTTON_X_OFFSET;
        int y = topPos + REROLL_BUTTON_Y_OFFSET;

        if (canReroll && canAfford) {
            int mouseDiffX = mouseX - x;
            int mouseDiffY = mouseY - y;

            ResourceLocation rerollTexture;

            if (mouseDiffX >= 0 && mouseDiffY >= 0 && mouseDiffX < REROLL_BUTTON_WIDTH && mouseDiffY < REROLL_BUTTON_HEIGHT) {
                rerollTexture = REROLL_HIGHLIGHTED_TEXTURE;
            } else {
                rerollTexture = REROLL_TEXTURE;
            }

            graphics.blit(rerollTexture, x, y, 0, 0,
                    REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT,
                    REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT
            );

            graphics.blitSprite(ENABLED_LEVEL_SPRITES[costIndex],
                    x + REROLL_COST_X_OFFSET, y + REROLL_COST_Y_OFFSET,
                    REROLL_COST_WIDTH, REROLL_COST_HEIGHT
            );
        } else {
            graphics.blit(REROLL_DISABLED_TEXTURE, x, y, 0, 0,
                    REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT,
                    REROLL_BUTTON_WIDTH, REROLL_BUTTON_HEIGHT
            );

            if (canReroll) {
                // Don't render the cost if the reroll is disabled due to the source enchantability
                graphics.blitSprite(DISABLED_LEVEL_SPRITES[costIndex],
                        x + REROLL_COST_X_OFFSET, y + REROLL_COST_Y_OFFSET,
                        REROLL_COST_WIDTH, REROLL_COST_HEIGHT
                );
            }
        }

        RenderSystem.disableBlend();
    }

    private void renderEnchantmentSlot(
            @NotNull final GuiGraphics graphics,
            final int mouseX,
            final int mouseY,
            final Holder<Enchantment> enchantment,
            final int buttonIndex,
            final int buttonX,
            final int buttonY
    ) {
        int enchantingPower = menu.enchantingPower[buttonIndex];
        int level = menu.levelClue[buttonIndex];

        int cost = menu.calculateEnchantmentCost(enchantment);

        EnchantmentSource source = EnchantmentSource.byId(menu.enchantmentSources[buttonIndex]);

        boolean usePlain = Config.BINARY_ACCESSIBILITY_USE_PLAIN_BACKGROUND.get();
        Component name = Enchantment.getFullname(enchantment, level);

        ResourceLocation enabled = usePlain
                ? source.getEnabledTexture()
                : Common.resource(PREFIX + "enabled/" + source.getName() + "_" + menu.tableTextureIndices[buttonIndex] + ".png");

        ResourceLocation highlighted = usePlain
                ? source.getHighlightedTexture()
                : Common.resource(PREFIX + "highlighted/" + source.getName() + "_" + menu.tableTextureIndices[buttonIndex] + ".png");

        ResourceLocation disabled = usePlain
                ? source.getDisabledTexture()
                : Common.resource(PREFIX + "disabled/" + source.getName() + "_" + menu.tableTextureIndices[buttonIndex] + ".png");

        if (Config.BINARY_ACCESSIBILITY_OBFUSCATE_NEW_ENCHANTMENTS.get()) {
            name = name.copy().withStyle(name.getStyle().withFont(GALACTIC_FONT_ID));
        }

        int costIndex = Math.clamp(cost - 1, 0, 2);
        int textX = buttonX + ENCHANTING_TEXT_X_OFFSET;
        int textY = buttonY + ENCHANTING_TEXT_Y_OFFSET;

        int color;

        //noinspection DataFlowIssue -> player is present
        boolean canAfford = minecraft.player.hasInfiniteMaterials()
                || (getLapisAmount() >= cost && minecraft.player.experienceLevel >= enchantingPower && minecraft.player.experienceLevel >= cost);

        if (canAfford) {
            int mouseDiffX = mouseX - buttonX;
            int mouseDiffY = mouseY - buttonY;

            int nameColor;

            ResourceLocation resource;

            if (mouseDiffX >= 0 && mouseDiffY >= 0 && mouseDiffX < ENCHANTING_BUTTON_WIDTH && mouseDiffY < ENCHANTING_BUTTON_HEIGHT) {
                resource = highlighted;
                nameColor = source.getHighlightedColor();
            } else {
                resource = enabled;
                nameColor = source.getEnabledColor();
            }

            RenderSystem.enableBlend();
            graphics.blit(resource, buttonX, buttonY, 0, 0,
                    ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT,
                    ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT
            );

            graphics.blitSprite(ENABLED_LEVEL_SPRITES[costIndex],
                    buttonX + ENCHANTING_COST_X_OFFSET, buttonY + ENCHANTING_COST_Y_OFFSET,
                    ENCHANTING_COST_WIDTH, ENCHANTING_COST_HEIGHT
            );
            RenderSystem.disableBlend();

            if (Config.BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_NAME_COLOR.get()) {
                name = name.copy().withColor(nameColor);
            }

            graphics.drawWordWrap(font, name, textX, textY, ENCHANTING_TEXT_MAX_WIDTH, BLACK);
            color = ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR;
        } else {
            RenderSystem.enableBlend();
            graphics.blit(disabled, buttonX, buttonY, 0, 0,
                    ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT,
                    ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT
            );

            graphics.blitSprite(DISABLED_LEVEL_SPRITES[costIndex],
                    buttonX + ENCHANTING_COST_X_OFFSET, buttonY + ENCHANTING_COST_Y_OFFSET,
                    ENCHANTING_COST_WIDTH, ENCHANTING_COST_HEIGHT
            );
            RenderSystem.disableBlend();

            if (Config.BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_NAME_COLOR.get()) {
                name = name.copy().withColor(source.getDisabledColor());
            }

            graphics.drawWordWrap(font, name, textX, textY, ENCHANTING_TEXT_MAX_WIDTH, BLACK);
            color = ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR;
        }

        if (level < enchantment.value().getMaxLevel()) {
            String power = String.valueOf(enchantingPower);
            int textWidth = font.width(power);

            // Outline
            graphics.drawString(font, power, textX + ENCHANTING_POWER_X_OFFSET - textWidth + 1, buttonY + ENCHANTING_POWER_Y_OFFSET, BLACK, false);
            graphics.drawString(font, power, textX + ENCHANTING_POWER_X_OFFSET - textWidth - 1, buttonY + ENCHANTING_POWER_Y_OFFSET, BLACK, false);
            graphics.drawString(font, power, textX + ENCHANTING_POWER_X_OFFSET - textWidth, buttonY + ENCHANTING_POWER_Y_OFFSET + 1, BLACK, false);
            graphics.drawString(font, power, textX + ENCHANTING_POWER_X_OFFSET - textWidth, buttonY + ENCHANTING_POWER_Y_OFFSET - 1, BLACK, false);

            // Level requirement
            graphics.drawString(font, power, textX + ENCHANTING_POWER_X_OFFSET - textWidth, buttonY + ENCHANTING_POWER_Y_OFFSET, color, true);
        }
    }

    private void renderFullyUpgradedSlot(@NotNull final GuiGraphics graphics, final Holder<Enchantment> enchantment, final int level, final int buttonX, final int buttonY) {
        int textX = buttonX + 8;
        int textY = buttonY + ENCHANTING_TEXT_Y_OFFSET;

        Component name = Enchantment.getFullname(enchantment, level);

        ResourceLocation background = (level > enchantment.value().getMaxLevel())
                ? ENCHANTMENT_SLOT_OVER_ENCHANTED_TEXTURE
                : ENCHANTMENT_SLOT_MAXED_OUT_TEXTURE;

        graphics.blit(background, buttonX, buttonY, 0, 0,
                ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT,
                ENCHANTING_BUTTON_WIDTH, ENCHANTING_BUTTON_HEIGHT
        );

        if (Config.BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_NAME_COLOR.get()) {
            int color = (level > enchantment.value().getMaxLevel())
                    ? ENCHANTMENT_OVER_ENCHANTED_TEXT_COLOR
                    : ENCHANTMENT_MAXED_OUT_TEXT_COLOR;

            name = name.copy().withColor(color);
        }

        graphics.drawString(font, name, textX, textY, BLACK, false);
    }

    private int getLapisAmount() {
        return menu.getSlot(OverhauledEnchantmentMenu.LAPIS_SLOT).getItem().getCount();
    }

    /** The number of pixels to add to the screen height to fit the new GUI. */
    private static final int SCREEN_HEIGHT_ADJUSTMENT = 29;

    /** X-coordinate offset from the screen's left edge for the reroll button. */
    private static final int REROLL_BUTTON_X_OFFSET = 15 - 7;
    /** Y-coordinate offset from the screen's top edge for the reroll button. */
    private static final int REROLL_BUTTON_Y_OFFSET = 17;
    /** Width of the reroll button. */
    private static final int REROLL_BUTTON_WIDTH = 36;
    /** Height of the reroll button. */
    private static final int REROLL_BUTTON_HEIGHT = 18;
    /** Width of the cost icon on the reroll button. */
    private static final int REROLL_COST_WIDTH = 16;
    /** Height of the cost icon on the reroll button. */
    private static final int REROLL_COST_HEIGHT = 16;
    /** X-coordinate offset from the button's left edge for the cost icon. */
    private static final int REROLL_COST_X_OFFSET = -1;
    /** Y-coordinate offset from the button's top edge for the cost icon. */
    private static final int REROLL_COST_Y_OFFSET = 1;

    /** Width of the three main enchantment/upgrade/transfer buttons. */
    private static final int ENCHANTING_BUTTON_WIDTH = 108 + 7;
    /** Height of the three main enchantment/upgrade/transfer buttons. */
    private static final int ENCHANTING_BUTTON_HEIGHT = 18;
    /** X-coordinate offset from the screen's left edge for the enchantment buttons. */
    private static final int ENCHANTING_BUTTON_X_OFFSET = 60 - 7 - 1;
    /** Y-coordinate offset from the screen's top edge for the first enchantment button. */
    private static final int ENCHANTING_BUTTON_Y_OFFSET = 17 + 2;

    /** Width of the cost icon on the enchantment buttons. */
    private static final int ENCHANTING_COST_WIDTH = 16;

    /** X-coordinate offset from the button's left edge for the enchantment text. */
    private static final int ENCHANTING_TEXT_X_OFFSET = ENCHANTING_COST_WIDTH;
    /** Y-coordinate offset from the button's top edge for the enchantment text. */
    private static final int ENCHANTING_TEXT_Y_OFFSET = 5;
    /** Max width of the enchantment name text before clipping. */
    private static final int ENCHANTING_TEXT_MAX_WIDTH = ENCHANTING_BUTTON_WIDTH - ENCHANTING_TEXT_X_OFFSET - 2;


    /** Height of the cost icon on the enchantment buttons. */
    private static final int ENCHANTING_COST_HEIGHT = 16;
    /** X-coordinate offset from the button's left edge for the cost icon. */
    private static final int ENCHANTING_COST_X_OFFSET = 0;
    /** Y-coordinate offset from the button's top edge for the cost icon. */
    private static final int ENCHANTING_COST_Y_OFFSET = 2;

    /** X-coordinate offset from the button's left edge for the power level text. */
    private static final int ENCHANTING_POWER_X_OFFSET = 86 + 11;
    /** Y-coordinate offset from the button's top edge for the power level text. */
    private static final int ENCHANTING_POWER_Y_OFFSET = 8;

    /** X-coordinate offset from the screen's left edge for the experience bar. */
    private static final int EXPERIENCE_BAR_X_OFFSET = 60 - 1;
    /** Y-coordinate offset from the screen's top edge for the experience bar. */
    private static final int EXPERIENCE_BAR_Y_OFFSET = 80 + 5;
    /** Width of the experience bar. */
    private static final int EXPERIENCE_BAR_WIDTH = 102;
    /** Height of the experience bar. */
    private static final int EXPERIENCE_BAR_HEIGHT = 5;

    private static final int BLACK = 0;

    /** Text color for an enchantment that is already at its maximum level. */
    private static final int ENCHANTMENT_MAXED_OUT_TEXT_COLOR = 0xF2F09D;
    /** Text color for an enchantment that is already at its maximum level. */
    private static final int ENCHANTMENT_OVER_ENCHANTED_TEXT_COLOR = 0x4D2299;

    /** Text color for the level requirement number when affordable. */
    private static final int ENCHANTMENT_ENCHANTMENT_POWER_ENABLED_COLOR = 0x80FF20;
    /** Text color for the level requirement number when unaffordable. */
    private static final int ENCHANTMENT_ENCHANTMENT_POWER_DISABLED_COLOR = 0x408000;

    private static final ResourceLocation GALACTIC_FONT_ID = ResourceLocation.withDefaultNamespace("alt");
    private static final ResourceLocation[] ENABLED_LEVEL_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3")};
    private static final ResourceLocation[] DISABLED_LEVEL_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1_disabled"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2_disabled"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3_disabled")};

    /** Main texture for the GUI. */
    private static final ResourceLocation BACKGROUND_TEXTURE = Common.resource("textures/gui/container/enchanting_table/background.png");

    /** Texture for the "turn page" button in its normal state. */
    private static final ResourceLocation REROLL_TEXTURE = Common.resource("textures/gui/container/enchanting_table/button/enabled/reroll.png");
    /** Texture for the "turn page" button when hovered. */
    private static final ResourceLocation REROLL_HIGHLIGHTED_TEXTURE = Common.resource("textures/gui/container/enchanting_table/button/highlighted/reroll.png");
    /** Texture for the "turn page" button when disabled. */
    private static final ResourceLocation REROLL_DISABLED_TEXTURE = Common.resource("textures/gui/container/enchanting_table/button/disabled/reroll.png");

    /** Texture for an enchantment slot when the enchantment is at its maximum level. */
    private static final ResourceLocation ENCHANTMENT_SLOT_MAXED_OUT_TEXTURE = Common.resource("textures/gui/container/enchanting_table/button/maxed_out.png");
    /** Texture for an enchantment slot when the enchantment is above its maximum level. */
    private static final ResourceLocation ENCHANTMENT_SLOT_OVER_ENCHANTED_TEXTURE = Common.resource("textures/gui/container/enchanting_table/button/over_enchanted.png");

    /** Texture for the filled (active) part of the player's experience bar. */
    private static final ResourceLocation EXPERIENCE_BAR_ACTIVE = Common.resource("textures/gui/container/enchanting_table/experience_bar/full.png");
    /** Texture for the empty (inactive) background of the player's experience bar. */
    private static final ResourceLocation EXPERIENCE_BAR_INACTIVE = Common.resource("textures/gui/container/enchanting_table/experience_bar/empty.png");
}
