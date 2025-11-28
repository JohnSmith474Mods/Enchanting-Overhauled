package johnsmith.enchantingoverhauled.mixin.client.enchantment;

import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.platform.Services;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @Unique
    private static ChatFormatting enchanting_Overhauled$getFormattingForLevel(int level) {
        if (level > Config.BOUNDED_ENCHANTMENT_MAX_LEVEL.get()) {
            return ChatFormatting.LIGHT_PURPLE;
        }
        return switch (level) {
            case 1 -> ChatFormatting.WHITE;
            case 2 -> ChatFormatting.YELLOW;
            default -> ChatFormatting.AQUA;
        };
    }

    @Inject(method = "getFullname", at = @At("HEAD"), cancellable = true)
    private static void modifyEnchantmentName(Holder<Enchantment> holder, int level, CallbackInfoReturnable<Component> cir) {
        Enchantment enchantment = holder.value();
        Optional<Integer> nameColor = Optional.empty();

        if (Config.BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_NAME_COLOR.get()) {
            nameColor = Optional.of(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_NAME_COLOR_VALUE.get());
        } else {
            // Use EnchantmentLib helper
            Minecraft client = Minecraft.getInstance();
            if (client.level != null) {
                ResourceKey<EnchantmentTheme> themeKey = EnchantmentLib.getThemeKey(client.level.registryAccess(), holder);

                // Look up the theme to get its color
                Optional<Registry<EnchantmentTheme>> themeRegistry = Services.PLATFORM.getThemeRegistry(client.level.registryAccess());
                if (themeRegistry.isPresent()) {
                    Optional<Holder.Reference<EnchantmentTheme>> theme = themeRegistry.get().get(themeKey);
                    if (theme.isPresent()) {
                        nameColor = theme.get().value().colorCode();
                    }
                }
            }
        }

        // 2. Create Base Name
        MutableComponent baseName = enchantment.description().copy();
        nameColor.ifPresent(color -> baseName.withStyle(style -> style.withColor(color)));

        // 3. Handle Single-Level (Mending/Infinity)
        if (level == 1 && enchantment.getMaxLevel() == 1) {
            if (holder.is(EnchantmentTags.CURSE)) {
                baseName.withStyle(ChatFormatting.RED);
            }
            cir.setReturnValue(baseName);
            return;
        }

        // 4. Create Level Component (Rarity vs Config Override)
        MutableComponent finalName;
        if (level == 1) {
            finalName = baseName;
        } else {
            MutableComponent levelText = Component.literal(EnchantmentLib.toRoman(level));

            if (Config.BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_LEVEL_COLOR.get()) {
                // ACCESSIBILITY: Force user defined color
                levelText.withStyle(Style.EMPTY.withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_LEVEL_COLOR_VALUE.get()));
            } else {
                // STANDARD: Use rarity formatting
                levelText.withStyle(enchanting_Overhauled$getFormattingForLevel(level));
            }

            finalName = baseName.append(CommonComponents.SPACE).append(levelText);
        }

        // 5. Apply Cursed Formatting
        if (holder.is(EnchantmentTags.CURSE)) {
            finalName.withStyle(ChatFormatting.RED);
        }

        cir.setReturnValue(finalName);
    }
}