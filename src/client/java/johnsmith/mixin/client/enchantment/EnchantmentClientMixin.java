package johnsmith.mixin.client.enchantment;

import johnsmith.EnchantingOverhauled;
import johnsmith.api.enchantment.theme.EnchantmentTheme;
import johnsmith.api.enchantment.theme.accessor.EnchantmentThemeAccessor;
import johnsmith.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.enchantment.MagicProtectionEnchantment;
import johnsmith.lib.EnchantmentLib;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique; // NEW IMPORT
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Enchantment.class)
public abstract class EnchantmentClientMixin {

    @Shadow
    public abstract String getTranslationKey();
    @Shadow
    public abstract boolean isCursed();
    @Shadow
    public abstract int getMaxLevel();

    @Shadow
    public abstract Text getName(int level);

    /**
     * Helper method to get the rarity-based formatting for a given level.
     */
    @Unique
    private Formatting getFormattingForLevel(int level) {
        return switch (level) {
            // Rarity colors:
            case 1 -> Formatting.WHITE;       // Common
            case 2 -> Formatting.YELLOW;       // Uncommon
            case 3 -> Formatting.AQUA;         // Rare
            default -> Formatting.LIGHT_PURPLE; // Levels 4+ (Epic)
        };
    }

    /**
     * Overrides the name formatting for enchantments on the CLIENT.
     * This version styles the base name with the theme color and the
     * roman numeral with the rarity color.
     */
    @Inject(method = "getName(I)Lnet/minecraft/text/Text;", at = @At("HEAD"), cancellable = true)
    private void modifyEnchantmentName(int level, CallbackInfoReturnable<Text> cir) {

        // 1. Get the theme and color code
        EnchantmentThemeAccessor accessor = (EnchantmentThemeAccessor)(Object)this;
        RegistryKey<EnchantmentTheme> themeKey = accessor.getTheme();
        Optional<Integer> colorCode = Optional.empty(); // Changed from int to Optional
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world != null) {
            DynamicRegistryManager registryAccess = client.world.getRegistryManager();
            Registry<EnchantmentTheme> themeRegistry = registryAccess.get(EnchantmentThemeRegistry.THEME_REGISTRY_KEY);
            EnchantmentTheme theme = themeRegistry.get(themeKey);
            if (theme != null) {
                colorCode = theme.colorCode(); // This is now an Optional<Integer>
            }
        }

        // 2. Get the base translation key
        String baseKey = this.getTranslationKey();

        // Cast 'this' to Enchantment to perform instanceof check
        Enchantment self = (Enchantment)(Object)this;

        // This is necessary cheese, since minecraft won't apply the translation key-value override
        // Check if this is a ProtectionEnchantment
        if (self instanceof ProtectionEnchantment protectionEnchantment && !(self instanceof MagicProtectionEnchantment)) {
            // Check if it's the specific Type.ALL (base Protection)
            if (protectionEnchantment.protectionType == ProtectionEnchantment.Type.ALL) {
                // If so, remap its key to our mod's namespace
                baseKey = "enchantment." + EnchantingOverhauled.MOD_ID + ".protection";
            }
        }
        // Check if this is a DamageEnchantment
        if (self instanceof DamageEnchantment damageEnchantment) {
            // Check if this is Bane of Arthropods
            if (baseKey.contains("bane_of_arthropods")) {
                // If so, remap its key to our mod's namespace
                baseKey = "enchantment." + EnchantingOverhauled.MOD_ID + ".bane_of_arthropods";
            }
        }

        // 3. Create and style the Base Name component
        MutableText baseName = Text.translatable(baseKey);

        // --- Only apply theme color if it is PRESENT ---
        colorCode.ifPresent(color -> baseName.styled(style -> style.withColor(color)));
        // --- If colorCode is empty, we apply no color, letting the GUI handle it. ---

        // 4. Handle "Level 1 / Max Level 1" case (e.g., Mending)
        if (level == 1 && this.getMaxLevel() == 1) {
            if (this.isCursed()) {
                baseName.formatted(Formatting.RED); // Cursed overrides theme
            }
            cir.setReturnValue(baseName);
            return;
        }

        // 5. Apply level-based logic
        MutableText finalName;
        if (level == 1) {
            // Level 1 of a multi-level enchant
            finalName = baseName;
        } else {
            // This block handles levels 2+

            // Create the level text (e.g., "IV")
            MutableText levelText = Text.literal(EnchantmentLib.toRoman(level));

            // Style the level text based on rarity
            levelText.formatted(getFormattingForLevel(level));

            // Combine the two styled components
            finalName = baseName.append(ScreenTexts.SPACE).append(levelText);
        }

        // 6. Apply Cursed formatting (overrides all other colors)
        if (this.isCursed()) {
            finalName.formatted(Formatting.RED);
        }

        // 7. Return the new name
        cir.setReturnValue(finalName);
    }
}