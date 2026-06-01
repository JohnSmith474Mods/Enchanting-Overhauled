package johnsmith.enchantingoverhauled.mixin.client.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.platform.Services;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @Shadow
    @Final
    private Enchantment.EnchantmentDefinition definition;

    /**
     * Helper to resolve the correct color for the Enchantment Name.
     */
    @Unique
    private static int enchanting_Overhauled$resolveNameColor(Holder<Enchantment> holder) {
        // 1. Config Override
        if (Config.BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_NAME_COLOR.get()) {
            return Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_NAME_COLOR_VALUE.get();
        }

        // 2. Theme Color (via Lib/Platform)
        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            ResourceKey<EnchantmentTheme> themeKey = EnchantmentLib.getThemeKey(client.level.registryAccess(), holder);
            var themeRegistry = Services.PLATFORM.getThemeRegistry(client.level.registryAccess());

            if (themeRegistry.isPresent()) {
                var theme = themeRegistry.get().get(themeKey);
                if (theme != null && theme.colorCode().isPresent()) {
                    return theme.colorCode().get();
                }
            }
        }

        // 3. Fallback
        return ChatFormatting.GRAY.getColor().intValue();
    }

    @Unique
    private static ChatFormatting enchanting_Overhauled$getFormattingForLevel(int level, Enchantment.EnchantmentDefinition definition) {
        if (level > definition.maxLevel()) {
            return ChatFormatting.LIGHT_PURPLE;
        }
        return switch (level) {
            case 1 -> ChatFormatting.WHITE;
            case 2 -> ChatFormatting.YELLOW;
            default -> ChatFormatting.AQUA;
        };
    }

    @WrapOperation(method = "getFullname",
                       at = @At(value = "INVOKE",
                               target = "Lnet/minecraft/network/chat/ComponentUtils;mergeStyles(Lnet/minecraft/network/chat/MutableComponent;Lnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/MutableComponent;",
                              ordinal = 1))
    private static MutableComponent enchanting_Overhauled$wrapNameStyle(
            MutableComponent component,
            Style style,
            Operation<MutableComponent> original,
            // 1. Manually append the arguments from the Enclosing Method (getFullname)
            // Mixin will automatically populate these.
            Holder<Enchantment> holder,
            int level
    ) {
        if (!Config.BINARY_ENABLE_ENCHANTMENT_TOOLTIP_MODIFICATIONS.get() || !Config.BINARY_COLOR_ENCHANTMENT_NAME_BY_THEME.get()) {
            return original.call(component, style);
        }

        int color = enchanting_Overhauled$resolveNameColor(holder);

        // We replace the style with our own color
        return component.withStyle(Style.EMPTY.withColor(color));
    }

    @WrapOperation(method = "getFullname",
                       at = @At(value = "INVOKE",
                               target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
    private static MutableComponent enchanting_Overhauled$wrapLevelText(
            String key,
            Operation<MutableComponent> original,
            Holder<Enchantment> holder,
            int level
    ) {
        if (!Config.BINARY_ENABLE_ENCHANTMENT_TOOLTIP_MODIFICATIONS.get() || !Config.BINARY_COLOR_ENCHANTMENT_LEVEL_BY_LEVEL.get()) {
            return original.call(key);
        }

        MutableComponent levelText = level == 1 ? Component.empty()
                                                : Component.literal(EnchantmentLib.toRoman(level));

        if (Config.BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_LEVEL_COLOR.get()) {
            int color = Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_LEVEL_COLOR_VALUE.get();
            levelText.withStyle(Style.EMPTY.withColor(color));
        } else {
            levelText.withStyle(enchanting_Overhauled$getFormattingForLevel(level, holder.value().definition()));
        }

        return levelText;
    }
}