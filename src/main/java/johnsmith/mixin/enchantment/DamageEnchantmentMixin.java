package johnsmith.mixin.enchantment;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(DamageEnchantment.class)
public class DamageEnchantmentMixin {
    @Shadow
    @Final
    private Optional<TagKey<EntityType<?>>> applicableEntities;

    @Inject(method = "canAccept(Lnet/minecraft/enchantment/Enchantment;)Z",
                at = @At("HEAD"),
       cancellable = true)
    public void canAccept(Enchantment other, CallbackInfoReturnable<Boolean>cir) {
        cir.setReturnValue(other != (DamageEnchantment) (Object)this);
        return;
    }

    @Inject(method = "getAttackDamage(ILnet/minecraft/entity/EntityType;)F",
                at = @At("HEAD"),
       cancellable = true)
    public void getAttackDamage(int level, @Nullable EntityType<?> entityType, CallbackInfoReturnable<Float>cir) {
        float baseDamageBonus = 1.5F;
        float diminishingReturns = 0.5F;
        float damageBonus = 0;
        for (int i = 0; i < level; i++) {
            damageBonus += baseDamageBonus;
            baseDamageBonus = Math.clamp(baseDamageBonus - diminishingReturns, 0.5F, Float.MAX_VALUE);
        }
        if (this.applicableEntities.isEmpty()) {
            cir.setReturnValue(damageBonus);
            return;
        } else {
            cir.setReturnValue(entityType != null && entityType.isIn(this.applicableEntities.get()) ? damageBonus * 4.0F : 0.0F);
            return;
        }
    }
}
