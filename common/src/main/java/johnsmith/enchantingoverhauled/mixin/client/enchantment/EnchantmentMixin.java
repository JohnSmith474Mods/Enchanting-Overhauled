package johnsmith.enchantingoverhauled.mixin.client.enchantment;

import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.accessor.EnchantmentThemeAccessor;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.platform.Services;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @Shadow
    public abstract String getDescriptionId();

    @Shadow
    public abstract boolean isCurse();

    @Shadow
    public abstract int getMaxLevel();

    @Shadow
    public abstract Component getFullname(int level);

    /**
     * Helper method to get the rarity-based formatting for a given level.
     * <p>
     * LIGHT_PURPLE (Epic) is only applied if the level exceeds the configured maximum.
     * Levels 1-2 have specific colors, and all other valid levels (up to max) are AQUA (Rare).
     */
    @Unique
    private ChatFormatting getFormattingForLevel(int level) {
        // If the level exceeds the natural limit set in config, mark it as Epic (Light Purple)
        if (level > Config.ENCHANTMENT_MAX_LEVEL) {
            return ChatFormatting.LIGHT_PURPLE;
        }

        // Otherwise, use the standard progression up to the max level
        return switch (level) {
            case 1 -> ChatFormatting.WHITE;   // Common
            case 2 -> ChatFormatting.YELLOW;  // Uncommon
            default -> ChatFormatting.AQUA;   // Rare (Level 3 up to Max)
        };
    }

    /**
     * Overrides the name formatting for enchantments on the CLIENT.
     * This version styles the base name with the theme color and the
     * roman numeral with the rarity color.
     */
    @Inject(method = "getFullname(I)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
    private void modifyEnchantmentName(int level, CallbackInfoReturnable<Component> cir) {

        // 1. Get the theme and color code
        EnchantmentThemeAccessor accessor = (EnchantmentThemeAccessor) (Object) this;
        ResourceKey<EnchantmentTheme> themeKey = accessor.enchanting_overhauled$getTheme();
        Optional<Integer> colorCode = Optional.empty();

        if (Config.OVERRIDE_ENCHANTMENT_NAME_COLORING) {
            colorCode = Optional.of(Config.OVERRIDE_ENCHANTMENT_NAME_COLOR);
        } else {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null) {
                RegistryAccess registryAccess = client.level.registryAccess();
                // Use platform helper to get the registry safely
                Optional<Registry<EnchantmentTheme>> themeRegistry = Services.PLATFORM.getThemeRegistry(registryAccess);

                if (themeRegistry.isPresent()) {
                    EnchantmentTheme theme = themeRegistry.get().get(themeKey);
                    if (theme != null) {
                        colorCode = theme.colorCode();
                    }
                }
            }
        }

        // 2. Get the base translation key
        String baseKey = this.getDescriptionId();

        // Cast 'this' to Enchantment to perform instanceof check
        Enchantment self = (Enchantment) (Object) this;

        // 3. Create and style the Base Name component
        MutableComponent baseName = Component.translatable(baseKey);

        // --- Only apply theme color if it is PRESENT ---
        colorCode.ifPresent(color -> baseName.withStyle(style -> style.withColor(color)));
        // --- If colorCode is empty, we apply no color, letting the GUI handle it. ---

        // 4. Handle "Level 1 / Max Level 1" case (e.g., Mending)
        if (level == 1 && this.getMaxLevel() == 1) {
            if (this.isCurse()) {
                baseName.withStyle(ChatFormatting.RED); // Cursed overrides theme
            }
            cir.setReturnValue(baseName);
            return;
        }

        // 5. Apply level-based logic
        MutableComponent finalName;
        if (level == 1) {
            // Level 1 of a multi-level enchant
            finalName = baseName;
        } else {
            // This block handles levels 2+

            // Create the level text (e.g., "IV")
            MutableComponent levelText = Component.literal(EnchantmentLib.toRoman(level));

            if (Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING) {
                levelText.withStyle(Style.EMPTY.withColor(Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR));
            } else {
                // Style the level text based on rarity
                levelText.withStyle(getFormattingForLevel(level));
            }


            // Combine the two styled components
            finalName = baseName.append(CommonComponents.SPACE).append(levelText);
        }

        // 6. Apply Cursed formatting (overrides all other colors)
        if (this.isCurse()) {
            finalName.withStyle(ChatFormatting.RED);
        }

        // 7. Return the new name
        cir.setReturnValue(finalName);
    }
}