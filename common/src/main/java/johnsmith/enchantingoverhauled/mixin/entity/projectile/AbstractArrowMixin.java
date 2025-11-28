package johnsmith.enchantingoverhauled.mixin.entity.projectile;

import johnsmith.enchantingoverhauled.api.enchantment.accessor.FireDurationAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput; // [New Import]
import net.minecraft.world.level.storage.ValueOutput; // [New Import]
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Entity implements FireDurationAccessor {

    @Unique
    private float enchanting_overhauled$fireDuration = -1;

    @Shadow
    public abstract boolean isCritArrow();

    public AbstractArrowMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void enchanting_overhauled$setFireDuration(float seconds) {
        this.enchanting_overhauled$fireDuration = seconds;
    }

    @Override
    public float enchanting_overhauled$getFireDuration() {
        return this.enchanting_overhauled$fireDuration;
    }

    // [Fix 1] Updated to use ValueOutput instead of CompoundTag
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveFireDuration(ValueOutput output, CallbackInfo ci) {
        if (this.enchanting_overhauled$fireDuration > -1) {
            output.putFloat("EnchantingOverhauledFireDuration", this.enchanting_overhauled$fireDuration);
        }
    }

    // [Fix 2] Updated to use ValueInput instead of CompoundTag
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void loadFireDuration(ValueInput input, CallbackInfo ci) {
        // 'getFloatOr' is the standard pattern for ValueInput based on other methods like getDoubleOr
        // We use -1.0F as the default to indicate "not set"
        this.enchanting_overhauled$fireDuration = input.getFloatOr("EnchantingOverhauledFireDuration", -1.0F);
    }

    /**
     * Redirects the vanilla "setSecondsOnFire(5)" call in onHitEntity.
     * Uses our custom duration if available.
     */
    @Redirect(
            method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"
            )
    )
    private void applyCustomFireDuration(Entity target, float vanillaSeconds) {
        if (this.enchanting_overhauled$fireDuration > -1) {
            // Use our configured duration
            target.igniteForSeconds(this.enchanting_overhauled$fireDuration);
        } else {
            // Fallback to vanilla (5 seconds) if no effect was applied
            target.igniteForSeconds(vanillaSeconds);
        }
    }
}