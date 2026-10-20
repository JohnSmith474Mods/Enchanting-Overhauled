package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.Config;

import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.api.config.type.ColoringMode;

import johnsmith.enchantmentcore.api.tooltip.DynamicDescriptionFormatter;
import johnsmith.enchantmentcore.api.tooltip.formatting.StyleModifier;
import johnsmith.enchantmentcore.api.util.RomanNumeralConverter;
import johnsmith.enchantmentcore.api.registry.EnchantmentEffectComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A centralized utility class for formatting GUI and Tooltip text components
 * according to the mod's configuration standards.
 */
public class UIFormattingUtil {
    public static final ResourceLocation GALACTIC_FONT = ResourceLocation.fromNamespaceAndPath("minecraft", "alt");

    /**
     * Formats an enchantment name and level into a single Component, applying obfuscation and theme colors.
     */
    public static MutableComponent formatEnchantmentName(Holder<Enchantment> enchantment, int level, boolean obfuscate, ColoringMode themeMode, ColoringMode levelMode, boolean showFirstLevel) {
        MutableComponent nameComponent = enchantment.value().description().copy();
        Style nameStyle = Style.EMPTY;
        Style levelStyle = Style.EMPTY;

        if (obfuscate) {
            nameStyle = nameStyle.withFont(GALACTIC_FONT);
            levelStyle = levelStyle.withFont(GALACTIC_FONT);
        }

        if (themeMode != ColoringMode.NONE) {
            nameStyle = nameStyle.withColor(getColor(enchantment, themeMode));
        }

        if (levelMode != ColoringMode.NONE) {
            levelStyle = levelStyle.withColor(getLevelColor(enchantment, level, levelMode));
        }

        nameComponent.withStyle(nameStyle);

        if (level != 1 || showFirstLevel && enchantment.value().getMaxLevel() != 1) {
            MutableComponent levelComponent = Component.literal(RomanNumeralConverter.toRoman(level)).withStyle(levelStyle);
            nameComponent.append(CommonComponents.SPACE).append(levelComponent);
        }

        return nameComponent;
    }

    /**
     * Formats an enchantment's description, resolving dynamic arguments, indentation, and wrapping.
     */
    public static List<Component> formatEnchantmentDescription(Holder<Enchantment> enchantment, int level, boolean obfuscate, ColoringMode themeMode, ColoringMode levelMode) {
        if (!Config.ENABLE_DESCRIPTION.get()) return new ArrayList<>();

        String baseKey = enchantment.unwrapKey().map(k -> Util.makeDescriptionId("enchantment", k.location())).orElse("");
        if (baseKey.isEmpty()) return new ArrayList<>();

        String preciseKey = baseKey + ".description.precise";
        String vagueKey = baseKey + ".description.vague";

        String descriptionString = Component.translatable(preciseKey).getString();
        if (descriptionString.equals(preciseKey)) {
            descriptionString = Component.translatable(vagueKey).getString();
            if (descriptionString.equals(vagueKey) || descriptionString.isEmpty()) {
                return new ArrayList<>();
            }
        }

        if (descriptionString.isEmpty()) return new ArrayList<>();

        Map<String, LevelBasedValue> args = enchantment.value().effects().get(EnchantmentEffectComponents.DESCRIPTION_ARGS);

        Style descStyle = Style.EMPTY.withColor(Config.DESCRIPTION_COLOR.get());
        if (obfuscate) {
            descStyle = descStyle.withFont(GALACTIC_FONT);
        }

        Map<String, StyleModifier> contextStyles = new HashMap<>();
        contextStyles.put("theme", (style, arg) -> style.withColor(getColor(enchantment, themeMode)));
        contextStyles.put("level", (style, arg) -> style.withColor(getLevelColor(enchantment, level, levelMode)));

        MutableComponent root = DynamicDescriptionFormatter.parseAndFormat(descriptionString, level, enchantment.value().getMaxLevel(), args, descStyle, contextStyles);

        return DynamicDescriptionFormatter.wrapRich(root, Config.DESCRIPTION_MAX_LINE_WIDTH.get(), Config.DESCRIPTION_INDENTATION_DEPTH.get());
    }

    /**
     * Resolves the primary text color for an enchantment based on its theme.
     */
    public static int getColor(Holder<Enchantment> enchantment, ColoringMode mode) {
        if (enchantment.is(EnchantmentTags.CURSE) && mode == ColoringMode.ENCHANTMENT) {
            Integer color = ChatFormatting.RED.getColor();
            return color != null ? color : 0xFF0000;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            ResourceKey<EnchantmentTheme> themeKey = EnchantmentThemes.getThemeKey(client.level.registryAccess(), enchantment);
            var themeRegistry = client.level.registryAccess().registry(EnchantmentThemes.THEME_REGISTRY_KEY);

            if (themeRegistry.isPresent()) {
                var theme = themeRegistry.get().get(themeKey);
                if (theme != null && theme.colorCode().isPresent()) {
                    return theme.colorCode().get();
                }
            }
        }

        Integer color = ChatFormatting.GRAY.getColor();
        return color != null ? color : 0xFFAAAAAA;
    }

    /**
     * Resolves the level text color for an enchantment based on its value.
     */
    public static int getLevelColor(Holder<Enchantment> enchantment, int level, ColoringMode mode) {
        if (enchantment.is(EnchantmentTags.CURSE) && mode == ColoringMode.ENCHANTMENT) {
            Integer color = ChatFormatting.RED.getColor();
            return color != null ? color : 0xFFFF5555;
        }

        Integer color = getFormattingForLevel(level, enchantment.value().definition()).getColor();
        return color != null ? color : 0xFFAAAAAA;
    }

    /**
     * Resolves the formatting for a specific level.
     */
    public static ChatFormatting getFormattingForLevel(int level, Enchantment.EnchantmentDefinition definition) {
        if (level > definition.maxLevel()) {
            return ChatFormatting.LIGHT_PURPLE;
        }
        return switch (level) {
            case 1 -> ChatFormatting.WHITE;
            case 2 -> ChatFormatting.YELLOW;
            default -> ChatFormatting.AQUA;
        };
    }
}