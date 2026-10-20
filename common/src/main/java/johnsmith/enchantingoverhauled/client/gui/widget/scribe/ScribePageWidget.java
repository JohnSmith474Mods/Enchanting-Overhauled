package johnsmith.enchantingoverhauled.client.gui.widget.scribe;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.client.UIFormattingUtil;
import johnsmith.enchantingoverhauled.client.gui.tooltip.scribe.ScribeTooltipContext;
import johnsmith.enchantingoverhauled.api.config.type.ColoringMode;
import johnsmith.enchantingoverhauled.api.config.type.FormatApplicationMode;
import johnsmith.enchantingoverhauled.api.config.type.ObfuscationLevel;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.menu.scribe.ScribeMenu;
import johnsmith.enchantingoverhauled.menu.scribe.controller.ScribeRules;
import johnsmith.enchantingoverhauled.menu.scribe.controller.ScribeInput;
import johnsmith.enchantingoverhauled.client.gui.sound.SoundEffectDataFactory;

import johnsmith.enchantingoverhauled.api.tag.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles the rendering, scrolling, and interaction of a single enchantment list page.
 */
public class ScribePageWidget {

    private final boolean isSource;
    private final ScribeMenu menu;

    private int leftX, topY;
    private int scrollOffset = 0;
    private ScribeTooltipContext hoveredTooltip = null;

    private record PageLine(int enchantId, boolean available, Holder<Enchantment> enchantment, int level, Component bullet, Component name) {}

    public ScribePageWidget(boolean isSource, ScribeMenu menu) {
        this.isSource = isSource;
        this.menu = menu;
    }

    public void updatePosition(int leftX, int topY) {
        this.leftX = leftX;
        this.topY = topY;
    }

    public ScribeTooltipContext getHoveredTooltip() {
        return this.hoveredTooltip;
    }

    private boolean shouldObfuscate() {
        ItemStack stack = this.isSource ? this.menu.getSlot(0).getItem() : this.menu.getSlot(2).getItem();
        if (stack.isEmpty()) return false;

        ObfuscationLevel obsLevel = Config.OBFUSCATION_LEVEL.get();
        if (obsLevel == ObfuscationLevel.NONE) return false;
        if (obsLevel == ObfuscationLevel.FULL) return true;

        boolean isFleeting = stack.is(Tags.FLEETING_ENCHANTMENT_SOURCE);
        boolean isPersistent = stack.is(Tags.PERSISTENT_ENCHANTMENT_SOURCE);

        if (obsLevel == ObfuscationLevel.FLEETING) return this.isSource && isFleeting;
        if (obsLevel == ObfuscationLevel.UNTAGGED) return !isPersistent;

        return false;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, Font font) {
        this.hoveredTooltip = null;

        ItemStack stack = this.isSource ? this.menu.getSlot(0).getItem() : this.menu.getSlot(2).getItem();
        if (stack.isEmpty()) return;

        boolean obfuscate = this.shouldObfuscate();

        Component title = Component.translatable(this.isSource ? "gui.enchanting_overhauled.scribe.source" : (this.menu.getGridData().isTome() ? "gui.enchanting_overhauled.scribe.contents" : "gui.enchanting_overhauled.scribe.index"));
        guiGraphics.drawString(font, title.copy().withStyle(Style.EMPTY.withColor(0x555555)), this.leftX, this.topY - 11, 0, false);

        List<Map.Entry<Holder<Enchantment>, Integer>> list = this.getDisplayEnchantments();
        List<PageLine> lines = this.buildPageLines(list, obfuscate);

        int visibleLines = 6;
        int itemHeight = 10;
        int maxWidth = 71;

        int maxScroll = Math.max(0, lines.size() - visibleLines);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));

        boolean canScrollUp = this.scrollOffset > 0;
        boolean canScrollDown = this.scrollOffset < maxScroll;

        Holder<Enchantment> selectedSourceEnch = null;
        int selectedId = this.menu.getGridData().getSelectedEnchantmentIndex();

        if (selectedId >= 0 && !this.menu.getSlot(0).getItem().isEmpty()) {
            Registry<Enchantment> registry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            for (var entry : EnchantmentLib.getEnchantments(this.menu.getSlot(0).getItem()).entrySet()) {
                if (registry.getId(entry.getKey().value()) == selectedId) {
                    selectedSourceEnch = entry.getKey();
                    break;
                }
            }
        }

        for (int i = 0; i < Math.min(visibleLines, lines.size()); i++) {
            int y = this.topY + (i * itemHeight);

            if (i == 0 && canScrollUp) {
                Component dots = Component.literal("\u2022 ").withColor(0xFF555555);
                guiGraphics.drawString(font, dots, this.leftX + (maxWidth / 2) - (font.width(dots) / 2), y, 0, false);
                continue;
            }
            if (i == visibleLines - 1 && canScrollDown) {
                Component dots = Component.literal("\u2022 ").withColor(0xFF555555);
                guiGraphics.drawString(font, dots, this.leftX + (maxWidth / 2) - (font.width(dots) / 2), y, 0, false);
                continue;
            }

            int itemIndex = this.scrollOffset + i;
            PageLine line = lines.get(itemIndex);

            boolean selected;
            if (this.isSource) {
                selected = line.enchantId() == selectedId;
            } else {
                selected = line.enchantment().equals(selectedSourceEnch);
            }

            boolean hovered = mouseX >= this.leftX && mouseX < this.leftX + maxWidth && mouseY >= y && mouseY < y + itemHeight;

            if (selected) {
                guiGraphics.fill(this.leftX - 2, y - 1, this.leftX + maxWidth, y + itemHeight - 1, 0x400000FF);
            }

            if (hovered) {
                int anchorX = this.isSource ? this.leftX + maxWidth : this.leftX;
                this.hoveredTooltip = new ScribeTooltipContext(line.enchantment(), line.level(), this.isSource, anchorX, y, obfuscate);
            }

            int bulletWidth = font.width(line.bullet());
            guiGraphics.drawString(font, line.bullet(), this.leftX, y, 0xFFFFFF, false);

            this.drawScrollingString(guiGraphics, font, line.name(), this.leftX + bulletWidth, y, this.leftX + maxWidth, y + itemHeight);
        }
    }

    public int mouseClicked(double mouseX, double mouseY) {
        if (!this.isSource) return -1;

        ItemStack stack = this.menu.getSlot(0).getItem();
        if (stack.isEmpty()) return -1;

        int visibleLines = 6;
        int itemHeight = 10;
        int maxWidth = 71;

        if (mouseX >= this.leftX && mouseX < this.leftX + maxWidth && mouseY >= this.topY && mouseY < this.topY + (visibleLines * itemHeight)) {

            ItemEnchantments enchants = EnchantmentLib.getEnchantments(stack);
            List<Map.Entry<Holder<Enchantment>, Integer>> list = new ArrayList<>(enchants.entrySet());

            int maxScroll = Math.max(0, list.size() - visibleLines);
            this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));

            boolean canScrollUp = this.scrollOffset > 0;
            boolean canScrollDown = this.scrollOffset < maxScroll;

            int clickedLineOffset = (int) (mouseY - this.topY) / itemHeight;

            if ((clickedLineOffset == 0 && canScrollUp) || (clickedLineOffset == visibleLines - 1 && canScrollDown)) {
                // Ignore clicks directly on the navigation dots
            } else {
                int lineIndex = this.scrollOffset + clickedLineOffset;
                if (lineIndex >= 0 && lineIndex < list.size()) {
                    var entry = list.get(lineIndex);
                    if (this.isEnchantmentAvailable(entry.getKey(), entry.getValue())) {
                        Registry<Enchantment> registry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                        int enchId = registry.getId(entry.getKey().value());
                        return new ScribeInput(enchId, 0, ScribeInput.ACTION_SELECT_ENCHANTMENT).encode();
                    }
                }
            }
        }
        return -1;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        int pageWidth = 78;
        int pageHeight = 85;
        int hitboxX = this.isSource ? this.leftX - 10 : this.leftX;

        if (mouseX >= hitboxX && mouseX < hitboxX + pageWidth && mouseY >= this.topY - 11 && mouseY < this.topY - 11 + pageHeight) {
            if (verticalAmount > 0) {
                if (this.scrollOffset > 0) {
                    this.scrollOffset--;
                    SoundEffectDataFactory.create(SoundEvents.BOOK_PAGE_TURN, 1.2F, 0.3F).play();
                }
            } else if (verticalAmount < 0) {
                if (!this.getDisplayEnchantments().isEmpty()) {
                    int maxScroll = Math.max(0, this.getDisplayEnchantments().size() - 6);
                    if (this.scrollOffset < maxScroll) {
                        this.scrollOffset++;
                        SoundEffectDataFactory.create(SoundEvents.BOOK_PAGE_TURN, 1.2F, 0.3F).play();
                    }
                }
            }
            return true;
        }
        return false;
    }

    private List<Map.Entry<Holder<Enchantment>, Integer>> getDisplayEnchantments() {
        ItemStack targetStack = this.menu.getSlot(2).getItem();
        ItemStack sourceStack = this.menu.getSlot(0).getItem();

        if (this.isSource) {
            if (sourceStack.isEmpty()) return List.of();
            return new ArrayList<>(EnchantmentLib.getEnchantments(sourceStack).entrySet());
        } else {
            if (targetStack.isEmpty()) return List.of();

            ItemEnchantments targetEnchants = EnchantmentLib.getEnchantments(targetStack);

            int selectedId = this.menu.getGridData().getSelectedEnchantmentIndex();
            if (selectedId >= 0 && !sourceStack.isEmpty()) {
                ItemEnchantments sourceEnchants = EnchantmentLib.getEnchantments(sourceStack);
                Registry<Enchantment> registry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

                Holder<Enchantment> sourceEnch = null;
                int sourceLevel = 0;

                for (var entry : sourceEnchants.entrySet()) {
                    if (registry.getId(entry.getKey().value()) == selectedId) {
                        sourceEnch = entry.getKey();
                        sourceLevel = entry.getIntValue();
                        break;
                    }
                }

                if (sourceEnch != null && this.isEnchantmentAvailable(sourceEnch, sourceLevel)) {
                    int targetLevel = targetEnchants.getLevel(sourceEnch);
                    int vanillaMax = sourceEnch.value().getMaxLevel();
                    boolean isTome = this.menu.getGridData().isTome();

                    int projectedLevel = targetLevel == 0 ? sourceLevel : targetLevel + sourceLevel;
                    boolean shouldUpgradeToTome = isTome;

                    if (!isTome) {
                        if (targetLevel == 0) {
                            shouldUpgradeToTome = ScribeRules.willConvertOnAppend(targetEnchants.size() + 1);
                        } else {
                            shouldUpgradeToTome = ScribeRules.willConvertOnUpgrade(projectedLevel, vanillaMax);
                        }
                    }

                    int absoluteMax = (shouldUpgradeToTome && Config.CAN_TOMES_HAVE_GREATER_ENCHANTMENTS.get()) ? vanillaMax + 1 : vanillaMax;
                    int newLevel = Math.min(projectedLevel, absoluteMax);

                    ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(targetEnchants);
                    mutable.set(sourceEnch, newLevel);
                    return new ArrayList<>(mutable.toImmutable().entrySet());
                }
            }

            return new ArrayList<>(targetEnchants.entrySet());
        }
    }

    private List<PageLine> buildPageLines(List<Map.Entry<Holder<Enchantment>, Integer>> enchantments, boolean obfuscate) {
        List<PageLine> allLines = new ArrayList<>();
        Registry<Enchantment> registry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        FormatApplicationMode mode = Config.SCRIBE_MENU_FORMAT_APPLICATION_MODE.get();
        ColoringMode themeColoring = (mode == FormatApplicationMode.WIDGET || mode == FormatApplicationMode.ALL) ? Config.SCRIBE_MENU_THEME_COLORING.get() : ColoringMode.NONE;
        ColoringMode levelColoring = (mode == FormatApplicationMode.WIDGET || mode == FormatApplicationMode.ALL) ? Config.SCRIBE_MENU_LEVEL_COLORING.get() : ColoringMode.NONE;
        boolean showFirstLevel = (mode == FormatApplicationMode.WIDGET || mode == FormatApplicationMode.ALL) && Config.SCRIBE_MENU_SHOW_FIRST_LEVEL.get();

        for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments) {
            int enchId = registry.getId(entry.getKey().value());
            MutableComponent name = UIFormattingUtil.formatEnchantmentName(entry.getKey(), entry.getValue(), obfuscate, themeColoring, levelColoring, showFirstLevel);
            boolean available = !this.isSource || this.isEnchantmentAvailable(entry.getKey(), entry.getValue());

            Style baseStyle = Style.EMPTY.withColor(available ? 0x333333 : 0xAA0000);
            Component bullet = Component.literal("  ").withStyle(baseStyle);

            if (!available) {
                name.withStyle(baseStyle);
                for (Component sibling : name.getSiblings()) {
                    if (sibling instanceof MutableComponent mutableSibling) {
                        mutableSibling.withStyle(baseStyle);
                    }
                }
            }

            allLines.add(new PageLine(enchId, available, entry.getKey(), entry.getValue(), bullet, name));
        }

        return allLines;
    }

    private boolean isEnchantmentAvailable(Holder<Enchantment> sourceEnch, int sourceLevel) {
        ItemStack book = this.menu.getSlot(2).getItem();
        if (book.isEmpty()) return false;

        ItemEnchantments targetEnchants = EnchantmentLib.getEnchantments(book);
        int targetLevel = targetEnchants.getLevel(sourceEnch);
        int maxEnchants = Config.MAX_ENCHANTMENT_AMOUNT.get();

        boolean isTome = this.menu.getGridData().isTome();

        if (targetLevel == 0) {
            return targetEnchants.size() < maxEnchants;
        } else {
            int vanillaMax = sourceEnch.value().getMaxLevel();
            int newLevel = targetLevel + sourceLevel;

            boolean willBeTome = isTome || ScribeRules.willConvertOnUpgrade(newLevel, vanillaMax);
            int absoluteMax = (willBeTome && Config.CAN_TOMES_HAVE_GREATER_ENCHANTMENTS.get()) ? vanillaMax + 1 : vanillaMax;

            return targetLevel < absoluteMax;
        }
    }

    private void drawScrollingString(GuiGraphics guiGraphics, Font font, Component text, int minX, int minY, int maxX, int maxY) {
        int textWidth = font.width(text);
        int maxWidth = maxX - minX;

        if (textWidth > maxWidth) {
            int overflow = textWidth - maxWidth;
            double time = (double) net.minecraft.Util.getMillis() / 1000.0D;
            double speed = Math.max((double) overflow * 0.5D, 3.0D);
            double scrollPos = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * time / speed)) / 2.0D + 0.5D;
            double offset = net.minecraft.util.Mth.lerp(scrollPos, 0.0D, (double) overflow);

            guiGraphics.enableScissor(minX, minY, maxX, maxY);
            guiGraphics.drawString(font, text, minX - (int) offset, minY, 0xFFFFFF, false);
            guiGraphics.disableScissor();
        } else {
            guiGraphics.drawString(font, text, minX, minY, 0xFFFFFF, false);
        }
    }
}