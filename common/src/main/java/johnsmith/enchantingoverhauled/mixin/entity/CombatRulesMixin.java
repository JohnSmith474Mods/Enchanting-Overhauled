package johnsmith.enchantingoverhauled.mixin.entity;

import johnsmith.enchantingoverhauled.config.Config;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CombatRules.class)
public class CombatRulesMixin {

    /**
     * Overrides the damage calculation for protection enchantments (EPF).
     * Replaces the vanilla divisor of 25.0F with a configurable value
     * to make Protection stronger, compensating for its chest-only application.
     *
     * Mapped: getInflictedDamage -> getDamageAfterAbsorb
     */
    @Inject(method = "getDamageAfterMagicAbsorb(FF)F", at = @At("HEAD"), cancellable = true)
    private static void onGetInflictedDamage(float damage, float protection, CallbackInfoReturnable<Float> cir) {

        // Get the divisor from config. Default to 25.0F (vanilla) if 0 or less.
        float divisor = Config.BOUNDED_PROTECTION_DENOMINATOR.get();

        // Clamp protection value (vanilla cap is 20)
        // Mapped: MathHelper.clamp -> Mth.clamp
        float f = Mth.clamp(protection, 0.0F, Config.BOUNDED_PROTECTION_NUMERATOR.get());

        // Calculate new damage using the configurable divisor
        float newDamage = damage * (1.0F - f / divisor);

        cir.setReturnValue(newDamage);
    }
}