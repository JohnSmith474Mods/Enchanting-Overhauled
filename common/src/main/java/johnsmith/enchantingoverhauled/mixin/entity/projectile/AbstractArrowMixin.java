package johnsmith.enchantingoverhauled.mixin.entity.projectile;

import johnsmith.enchantingoverhauled.api.enchantment.accessor.FireDurationAccessor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveFireDuration(CompoundTag compound, CallbackInfo ci) {
        if (this.enchanting_overhauled$fireDuration > -1) {
            compound.putFloat("EnchantingOverhauledFireDuration", this.enchanting_overhauled$fireDuration);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void loadFireDuration(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("EnchantingOverhauledFireDuration")) {
            this.enchanting_overhauled$fireDuration = compound.getInt("EnchantingOverhauledFireDuration");
        }
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