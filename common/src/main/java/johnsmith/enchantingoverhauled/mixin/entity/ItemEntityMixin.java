package johnsmith.enchantingoverhauled.mixin.entity;

import johnsmith.enchantingoverhauled.platform.Services;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        if (((ItemEntity) (Object) this).getItem().is(Services.PLATFORM.getEnchantedTome())) return true;
        return super.isInvulnerableTo(source);
    }
}
