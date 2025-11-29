package johnsmith.enchantingoverhauled.mixin.entity.item;

import johnsmith.enchantingoverhauled.platform.Services;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "hurtClient",
                at = @At("HEAD"),
       cancellable = true)
    private void makeTomeImmuneClient(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        // Prevents client-side prediction glitches (flashing red, disappearing briefly)
        if (((ItemEntity) (Object)this).getItem().is(Services.PLATFORM.getEnchantedTome())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hurtServer",
                at = @At("HEAD"),
       cancellable = true)
    private void makeTomeImmuneServer(ServerLevel level, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (((ItemEntity) (Object)this).getItem().is(Services.PLATFORM.getEnchantedTome())) {
            if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                return;
            } cir.setReturnValue(false);
        }
    }
}