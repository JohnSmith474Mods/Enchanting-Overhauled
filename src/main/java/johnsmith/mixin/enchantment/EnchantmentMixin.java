package johnsmith.mixin.enchantment;

import johnsmith.api.enchantment.theme.accessor.EnchantmentThemeAccessor;
import johnsmith.config.Config;
import johnsmith.api.enchantment.theme.EnchantmentTheme;
import johnsmith.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("all")
@Mixin(Enchantment.class)
public class EnchantmentMixin implements EnchantmentThemeAccessor {

    /**
     * Adds the theme field to all Enchantments.
     * We initialize it to DEFAULT to ensure no enchantment is ever missing a theme.
     */
    @Unique
    private RegistryKey<EnchantmentTheme> theme = null;

    /**
     * Implements the getter from our accessor.
     */
    @Override
    @Unique
    public RegistryKey<EnchantmentTheme> getTheme() {
        if (theme == null) {
            return EnchantmentThemeRegistry.DEFAULT;
        }
        return this.theme;
    }

    /**
     * Implements the setter from our accessor.
     */
    @Override
    @Unique
    public void setTheme(RegistryKey<EnchantmentTheme> theme) {
        this.theme = theme;
    }

    @Shadow
    @Final
    private Enchantment.Properties properties;

    @Inject(method = "getMaxLevel()I",
                at = @At("HEAD"),
       cancellable = true)
    public void getMaxLevel( CallbackInfoReturnable<Integer> cir) {
        if (this.properties.maxLevel() == 1) {
            cir.setReturnValue(this.properties.maxLevel());
        } else {
            cir.setReturnValue(Config.ENCHANTMENT_MAX_LEVEL);
        } return;
    }
}
