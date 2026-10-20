package johnsmith.enchantingoverhauled.client.tooltip;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.client.UIFormattingUtil;
import johnsmith.enchantingoverhauled.api.config.type.EnchantmentHeaderMode;
import johnsmith.enchantingoverhauled.api.config.type.ObfuscationLevel;
import johnsmith.enchantingoverhauled.api.tag.Tags;

import johnsmith.enchantmentcore.api.tooltip.EnchantmentTooltipFormatter;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OverhauledTooltipFormatter implements EnchantmentTooltipFormatter {

    @Override
    public boolean isEnabled() {
        return Config.ENABLE_TOOLTIP_MODIFICATIONS.get();
    }

    @Override
    public int getPriority() {
        return Config.TOOLTIP_FORMATTER_PRIORITY.get();
    }

    @Override
    public Optional<List<Component>> format(Holder<Enchantment> enchantment, int level, ItemStack stack) {
        List<Component> tooltip = new ArrayList<>();

        boolean isStored = stack.has(DataComponents.STORED_ENCHANTMENTS);
        boolean obfuscate = this.shouldObfuscate(stack, isStored);

        boolean conceilCurse = this.conceilCurse(enchantment, isStored);
        if (conceilCurse) return Optional.of(new ArrayList<>());

        this.injectHeader(tooltip, enchantment, stack, isStored);

        tooltip.add(UIFormattingUtil.formatEnchantmentName(enchantment, level, obfuscate, Config.THEME_COLORING.get(), Config.LEVEL_COLORING_LEVEL.get(), Config.SHOW_FIRST_LEVEL.get()));
        tooltip.addAll(UIFormattingUtil.formatEnchantmentDescription(enchantment, level, obfuscate, Config.THEME_COLORING.get(), Config.LEVEL_COLORING_LEVEL.get()));

        return Optional.of(tooltip);
    }

    private boolean conceilCurse(Holder<Enchantment> enchantment, boolean isStored) {
        if (!enchantment.is(EnchantmentTags.CURSE)) return false;

        return switch (Config.CURSE_CONCEALMENT_MODE.get()) {
            case NONE -> false;
            case APPLIED -> !isStored;
            case STORED -> isStored;
            case ALL -> true;
        };
    }

    private void injectHeader(List<Component> tooltip, Holder<Enchantment> enchantment, ItemStack stack, boolean isStored) {
        EnchantmentHeaderMode mode = Config.ENCHANTMENT_HEADER_MODE.get();
        if (mode == EnchantmentHeaderMode.NONE) return;
        if (isStored && mode == EnchantmentHeaderMode.APPLIED) return;
        if (!isStored && mode == EnchantmentHeaderMode.STORED) return;

        ItemEnchantments enchantments = stack.getOrDefault(isStored ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        // Only draw the header once by checking if the current enchantment is the first entry in the map
        if (!enchantments.isEmpty() && enchantments.entrySet().iterator().next().getKey().equals(enchantment)) {
            Component baseHeader = Component.translatable(isStored ? "item.enchanting_overhauled.stored_enchantments" : "item.enchanting_overhauled.applied_enchantments");
            tooltip.add(baseHeader.copy().withColor(Config.HEADER_COLOR_VALUE.get()));
        }
    }

    private boolean shouldObfuscate(ItemStack stack, boolean isStored) {
        if (!isStored || stack == null || stack.isEmpty()) return false;

        ObfuscationLevel obsLevel = Config.OBFUSCATION_LEVEL.get();
        if (obsLevel == ObfuscationLevel.NONE) return false;
        if (obsLevel == ObfuscationLevel.FULL) return true;

        boolean isFleeting = stack.is(Tags.FLEETING_ENCHANTMENT_SOURCE);
        boolean isPersistent = stack.is(Tags.PERSISTENT_ENCHANTMENT_SOURCE);

        if (obsLevel == ObfuscationLevel.FLEETING) return isFleeting;
        if (obsLevel == ObfuscationLevel.UNTAGGED) return isFleeting || !isPersistent;

        return false;
    }
}