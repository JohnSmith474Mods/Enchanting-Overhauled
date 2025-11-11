package johnsmith.mixin.block;

import org.spongepowered.asm.mixin.Mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {

    // This mixin allows the Frost Walker enchantment to let entities walk on powder
    // snow blocks.
    @Inject(method = "canWalkOnPowderSnow(Lnet/minecraft/entity/Entity;)Z",
                at = @At("HEAD"),
       cancellable = true)
    private static void canWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getType().isIn(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
            cir.setReturnValue(true);
        } else {
            boolean flag = false;
            if (entity instanceof LivingEntity) {
                ItemEnchantmentsComponent itemEnchantmentsComponent = ((LivingEntity) entity)
                        .getEquippedStack(EquipmentSlot.FEET).getEnchantments();
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentsMap()) {
                    final RegistryEntry<Enchantment> registryEntry = entry.getKey();
                    final Enchantment enchantment = (Enchantment) registryEntry.value();
                    if (enchantment instanceof FrostWalkerEnchantment) {
                        flag = true;
                    }
                }
            } cir.setReturnValue(
                    entity instanceof LivingEntity && (((LivingEntity) entity).getEquippedStack(EquipmentSlot.FEET).isOf(Items.LEATHER_BOOTS) || flag));
            return;
        }
    }
}