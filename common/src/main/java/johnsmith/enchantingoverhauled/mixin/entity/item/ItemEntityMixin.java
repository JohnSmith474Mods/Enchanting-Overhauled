package johnsmith.enchantingoverhauled.mixin.entity.item;

import johnsmith.enchantingoverhauled.platform.Services;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        if (((ItemEntity) (Object) this).getItem().is(Services.PLATFORM.getEnchantedTome())) return true;
        return super.isInvulnerableTo(source);
    }
}
