package johnsmith.enchantingoverhauled.client.gui.tooltip.enchant;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.client.UIFormattingUtil;
import johnsmith.enchantingoverhauled.api.config.type.ColoringMode;
import johnsmith.enchantingoverhauled.api.config.type.FormatApplicationMode;
import johnsmith.enchantingoverhauled.api.config.type.ObfuscationLevel;
import johnsmith.enchantingoverhauled.api.config.type.UpgradeSourceType;
import johnsmith.enchantmentcore.api.util.RomanNumeralConverter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * A sealed interface representing a command to render an enchantment tooltip.
 * <p>
 * Implements the Command Pattern, decoupling the tooltip state evaluation from
 * the actual GUI rendering logic. Specific implementations (like {@link Upgrade} or {@link MissingSource})
 * encapsulate the data context required to draw text dynamically on the screen.
 */
public sealed interface EnchantTooltip permits
        EnchantTooltip.Maxed,
        EnchantTooltip.Upgrade,
        EnchantTooltip.Transfer,
        EnchantTooltip.Apply,
        EnchantTooltip.MissingSource,
        EnchantTooltip.Empty {

    // region Constants & Helpers

    /**
     * The resource location referencing the standard galactic alphabet font used for table generation obfuscation.
     */
    ResourceLocation GALACTIC_FONT_ID = ResourceLocation.fromNamespaceAndPath("minecraft", "alt");

    /**
     * Executes the tooltip rendering logic.
     *
     * @param context The graphics context to render to.
     * @param mouseX  The current X position of the mouse pointer.
     * @param mouseY  The current Y position of the mouse pointer.
     * @return true if the tooltip was rendered, false if this command is empty.
     */
    boolean render(GuiGraphics context, int mouseX, int mouseY);

    /**
     * Appends standard cost indicators (Lapis and XP level requirements) to the provided formatted sequence list.
     * Skipped entirely if the player is in creative mode.
     *
     * @param list The target list of formatted sequences to mutate.
     * @param ctx  The evaluation context holding player state and costs.
     */
    static void drawTooltipCost(List<FormattedCharSequence> list, EnchantTooltipContext ctx) {
        if (!ctx.isCreative()) {
            list.add(FormattedCharSequence.EMPTY);

            int playerXp = ctx.minecraft() != null && ctx.minecraft().player != null ? ctx.minecraft().player.experienceLevel : 0;
            if (playerXp < ctx.powerRequirement()) {
                list.add(Component.translatable("container.enchant.level.requirement", ctx.powerRequirement()).withStyle(ChatFormatting.RED).getVisualOrderText());
            } else {
                MutableComponent mutableText = (ctx.cost() == 1) ? Component.translatable("container.enchant.lapis.one") : Component.translatable("container.enchant.lapis.many", ctx.cost());
                list.add(mutableText.withStyle(ctx.lapisCount() >= ctx.cost() ? ChatFormatting.GRAY : ChatFormatting.RED).getVisualOrderText());

                MutableComponent mutableText2 = (ctx.cost() == 1) ? Component.translatable("container.enchant.level.one") : Component.translatable("container.enchant.level.many", ctx.cost());
                list.add(mutableText2.withStyle(ChatFormatting.GRAY).getVisualOrderText());
            }
        }
    }

    static ColoringMode getThemeColoring() {
        FormatApplicationMode mode = Config.ENCHANTMENT_MENU_FORMAT_APPLICATION_MODE.get();
        return (mode == FormatApplicationMode.TOOLTIP || mode == FormatApplicationMode.ALL) ? Config.ENCHANTMENT_MENU_THEME_COLORING.get() : ColoringMode.NONE;
    }

    static ColoringMode getLevelColoring() {
        FormatApplicationMode mode = Config.ENCHANTMENT_MENU_FORMAT_APPLICATION_MODE.get();
        return (mode == FormatApplicationMode.TOOLTIP || mode == FormatApplicationMode.ALL) ? Config.ENCHANTMENT_MENU_LEVEL_COLORING.get() : ColoringMode.NONE;
    }

    static boolean getShowFirstLevel() {
        FormatApplicationMode mode = Config.ENCHANTMENT_MENU_FORMAT_APPLICATION_MODE.get();
        return (mode == FormatApplicationMode.TOOLTIP || mode == FormatApplicationMode.ALL) && Config.ENCHANTMENT_MENU_SHOW_FIRST_LEVEL.get();
    }
    // endregion

    // region Command Implementations

    /**
     * Tooltip Command: Renders standard information for an enchantment that has reached its maximum level.
     * Excludes cost components.
     *
     * @param ctx The tooltip context containing state data.
     */
    record Maxed(EnchantTooltipContext ctx) implements EnchantTooltip {
        @Override
        public boolean render(GuiGraphics context, int mouseX, int mouseY) {
            List<FormattedCharSequence> list = new ArrayList<>();

            list.add(UIFormattingUtil.formatEnchantmentName(ctx.enchantment(), ctx.level(), false, getThemeColoring(), getLevelColoring(), getShowFirstLevel()).getVisualOrderText());
            list.addAll(UIFormattingUtil.formatEnchantmentDescription(ctx.enchantment(), ctx.level(), false, getThemeColoring(), getLevelColoring()).stream().map(Component::getVisualOrderText).toList());

            context.renderTooltip(ctx.font(), list, mouseX, mouseY);
            return true;
        }
    }

    /**
     * Tooltip Command: Renders information for a pending upgrade on the target item, incrementing the displayed level
     * to reflect the projected outcome. Appends cost indicators.
     *
     * @param ctx The tooltip context containing state data.
     */
    record Upgrade(EnchantTooltipContext ctx) implements EnchantTooltip {
        @Override
        public boolean render(GuiGraphics context, int mouseX, int mouseY) {
            List<FormattedCharSequence> list = new ArrayList<>();

            list.add(Component.translatable("gui.enchanting_overhauled.upgrade").withColor(Config.HEADER_COLOR_VALUE.get()).getVisualOrderText());
            list.add(UIFormattingUtil.formatEnchantmentName(ctx.enchantment(), ctx.level() + 1, false, getThemeColoring(), getLevelColoring(), getShowFirstLevel()).getVisualOrderText());
            list.addAll(UIFormattingUtil.formatEnchantmentDescription(ctx.enchantment(), ctx.level() + 1, false, getThemeColoring(), getLevelColoring()).stream().map(Component::getVisualOrderText).toList());
            drawTooltipCost(list, ctx);

            context.renderTooltip(ctx.font(), list, mouseX, mouseY);
            return true;
        }
    }

    /**
     * Tooltip Command: Renders information for transferring an enchantment from a source book.
     * Does not mutate the displayed level, instead reflecting the exact level of the source material.
     *
     * @param ctx The tooltip context containing state data.
     */
    record Transfer(EnchantTooltipContext ctx) implements EnchantTooltip {
        @Override
        public boolean render(GuiGraphics context, int mouseX, int mouseY) {
            List<FormattedCharSequence> list = new ArrayList<>();

            list.add(Component.translatable("gui.enchanting_overhauled.transfer").withColor(Config.HEADER_COLOR_VALUE.get()).getVisualOrderText());
            list.add(UIFormattingUtil.formatEnchantmentName(ctx.enchantment(), ctx.level(), false, getThemeColoring(), getLevelColoring(), getShowFirstLevel()).getVisualOrderText());
            list.addAll(UIFormattingUtil.formatEnchantmentDescription(ctx.enchantment(), ctx.level(), false, getThemeColoring(), getLevelColoring()).stream().map(Component::getVisualOrderText).toList());
            drawTooltipCost(list, ctx);

            context.renderTooltip(ctx.font(), list, mouseX, mouseY);
            return true;
        }
    }

    /**
     * Tooltip Command: Renders a newly rolled enchantment generated by the enchanting table.
     * Optionally obfuscates the text and descriptions using the standard galactic alphabet
     * until hovered or decoded. Evaluates theme colors dynamically.
     *
     * @param ctx The tooltip context containing state data.
     */
    record Apply(EnchantTooltipContext ctx) implements EnchantTooltip {
        @Override
        public boolean render(GuiGraphics context, int mouseX, int mouseY) {
            List<FormattedCharSequence> list = new ArrayList<>();
            list.add(Component.translatable("gui.enchanting_overhauled.apply").withColor(Config.HEADER_COLOR_VALUE.get()).getVisualOrderText());

            ObfuscationLevel obsLevel = Config.OBFUSCATION_LEVEL.get();
            boolean obfuscate = obsLevel == ObfuscationLevel.FULL || obsLevel == ObfuscationLevel.UNTAGGED;

            list.add(UIFormattingUtil.formatEnchantmentName(ctx.enchantment(), ctx.level(), obfuscate, getThemeColoring(), getLevelColoring(), getShowFirstLevel()).getVisualOrderText());
            list.addAll(UIFormattingUtil.formatEnchantmentDescription(ctx.enchantment(), ctx.level(), obfuscate, getThemeColoring(), getLevelColoring()).stream().map(Component::getVisualOrderText).toList());

            drawTooltipCost(list, ctx);
            context.renderTooltip(ctx.font(), list, mouseX, mouseY);
            return true;
        }
    }

    /**
     * Tooltip Command: Renders an error-state tooltip when the user attempts an upgrade,
     * but the configured {@link UpgradeSourceType} rules demand a valid source catalyst that
     * is currently absent from the UI.
     *
     * @param ctx The tooltip context containing state data.
     */
    record MissingSource(EnchantTooltipContext ctx) implements EnchantTooltip {
        @Override
        public boolean render(GuiGraphics context, int mouseX, int mouseY) {
            List<FormattedCharSequence> list = new ArrayList<>();

            list.add(Component.translatable("gui.enchanting_overhauled.upgrade").withColor(Config.HEADER_COLOR_VALUE.get()).getVisualOrderText());
            list.add(UIFormattingUtil.formatEnchantmentName(ctx.enchantment(), ctx.level() + 1, false, getThemeColoring(), getLevelColoring(), getShowFirstLevel()).getVisualOrderText());
            list.addAll(UIFormattingUtil.formatEnchantmentDescription(ctx.enchantment(), ctx.level() + 1, false, getThemeColoring(), getLevelColoring()).stream().map(Component::getVisualOrderText).toList());
            drawTooltipCost(list, ctx);
            list.add(FormattedCharSequence.EMPTY);

            UpgradeSourceType sourceType = Config.UPGRADE_SOURCE_TYPE.get();
            int enchantmentLevel = sourceType == UpgradeSourceType.EQUAL ? ctx.level() : ctx.level() + 1;

            MutableComponent enchName = ctx.enchantment().value().description().copy().withColor(UIFormattingUtil.getColor(ctx.enchantment(), getThemeColoring()));
            MutableComponent levelText = Component.literal(RomanNumeralConverter.toRoman(enchantmentLevel)).withColor(UIFormattingUtil.getLevelColor(ctx.enchantment(), enchantmentLevel, getLevelColoring()));

            MutableComponent missingSourceText;
            if (sourceType == UpgradeSourceType.EQUAL) {
                missingSourceText = Component.translatable("gui.enchanting_overhauled.missing_source.equal", enchName, levelText).withStyle(ChatFormatting.RED);
            } else {
                missingSourceText = Component.translatable("gui.enchanting_overhauled.missing_source.greater", enchName, levelText).withStyle(ChatFormatting.RED);
            }

            list.addAll(ctx.font().split(missingSourceText, 200));
            context.renderTooltip(ctx.font(), list, mouseX, mouseY);

            return true;
        }
    }


    /**
     * Tooltip Command: Empty implementation used as a null object pattern when an invalid
     * or empty widget is hovered. Returns false to indicate no tooltip was drawn.
     */
    record Empty() implements EnchantTooltip {
        @Override
        public boolean render(GuiGraphics context, int mouseX, int mouseY) {
            return false;
        }
    }
    // endregion
}