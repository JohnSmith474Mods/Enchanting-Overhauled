package johnsmith.mixin.entity;

import johnsmith.config.Config; // Assuming your config class path
import net.minecraft.entity.DamageUtil;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageUtil.class)
public class DamageUtilMixin {

    /**
     * Overrides the damage calculation for protection enchantments (EPF).
     * Replaces the vanilla divisor of 25.0F with a configurable value
     * to make Protection stronger, compensating for its chest-only application.
     */
    @Inject(method = "getInflictedDamage(FF)F", at = @At("HEAD"), cancellable = true)
    private static void onGetInflictedDamage(float damageDealt, float protection, CallbackInfoReturnable<Float> cir) {

        // Get the divisor from config. Default to 25.0F (vanilla) if 0 or less.
        float divisor = (Config.PROTECTION_DIVISOR <= 0) ? 25.0F : Config.PROTECTION_DIVISOR.floatValue();

        // Clamp protection value (vanilla cap is 20)
        float f = MathHelper.clamp(protection, 0.0F, Config.PROTECTION_CAP.floatValue());

        // Calculate new damage using the configurable divisor
        float newDamage = damageDealt * (1.0F - f / divisor);

        cir.setReturnValue(newDamage);
    }
}