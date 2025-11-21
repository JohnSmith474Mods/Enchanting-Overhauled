package johnsmith.enchantingoverhauled.mixin.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {

    // This mixin allows the Frost Walker enchantment to let entities walk on powder
    // snow blocks.
    @Inject(method = "canEntityWalkOnPowderSnow(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void canEntityWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getType().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
            cir.setReturnValue(true);
        } else {
            boolean flag = false;
            if (entity instanceof LivingEntity) {
                ItemEnchantments itemEnchantmentsComponent = ((LivingEntity) entity)
                        .getItemBySlot(EquipmentSlot.FEET).getEnchantments();
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantmentsComponent.entrySet()) {
                    final Holder<Enchantment> registryEntry = entry.getKey();
                    final Enchantment enchantment = (Enchantment) registryEntry.value();
                    if (enchantment instanceof FrostWalkerEnchantment) {
                        flag = true;
                    }
                }
            } cir.setReturnValue(
                    entity instanceof LivingEntity && (((LivingEntity) entity).getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS) || flag));
            return;
        }
    }
}