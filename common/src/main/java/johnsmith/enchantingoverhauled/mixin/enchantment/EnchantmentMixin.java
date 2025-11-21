package johnsmith.enchantingoverhauled.mixin.enchantment;

import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.accessor.EnchantmentThemeAccessor;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeRegistry;

import johnsmith.enchantingoverhauled.config.Config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Enchantment.class)
public class EnchantmentMixin implements EnchantmentThemeAccessor {

    /**
     * Adds the theme field to all Enchantments.
     * We initialize it to DEFAULT to ensure no enchantment is ever missing a theme.
     */
    @Unique
    private ResourceKey<EnchantmentTheme> enchanting_overhauled$theme = EnchantmentThemeRegistry.DEFAULT;

    /**
     * Implements the getter from our accessor.
     */
    @Override
    @Unique
    public ResourceKey<EnchantmentTheme> enchanting_overhauled$getTheme() {
        return Objects.requireNonNullElse(enchanting_overhauled$theme, EnchantmentThemeRegistry.DEFAULT);
    }

    /**
     * Implements the setter from our accessor.
     */
    @Override
    @Unique
    public void enchanting_overhauled$setTheme(ResourceKey<EnchantmentTheme> theme) {
        this.enchanting_overhauled$theme = theme;
    }

    @Shadow
    @Final
    private Enchantment.EnchantmentDefinition definition;

    @Inject(method = "getMaxLevel()I",
                at = @At("HEAD"),
       cancellable = true)
    public void getMaxLevel( CallbackInfoReturnable<Integer> cir) {
        if (this.definition.maxLevel() == 1) {
            cir.setReturnValue(this.definition.maxLevel());
        } else {
            cir.setReturnValue(Config.ENCHANTMENT_MAX_LEVEL);
        } return;
    }
}
