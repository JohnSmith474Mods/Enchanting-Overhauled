package johnsmith.enchantingoverhauled.mixin.block;

import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.PowderSnowBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {

    @Inject(method = "canEntityWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
    private static void canEntityWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        // 1. Standard Vanilla Checks (Tags)
        if (entity.getType().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
            cir.setReturnValue(true);
            return;
        }

        if (entity instanceof LivingEntity living) {
            ItemStack feetStack = living.getItemBySlot(EquipmentSlot.FEET);

            // 2. Vanilla Leather Boots Check
            if (feetStack.is(Items.LEATHER_BOOTS)) {
                cir.setReturnValue(true);
                return;
            }

            // 3. Custom Effect Check: "Does this item have ANY enchantment with our component?"
            // We iterate the enchantments on the boots
            boolean hasWalkableEffect = EnchantmentHelper.getEnchantmentsForCrafting(feetStack)
                    .keySet()
                    .stream()
                    .anyMatch(holder -> holder.value().effects().has(EnchantmentEffectComponentRegistry.POWDER_SNOW_WALKABLE));

            if (hasWalkableEffect) {
                cir.setReturnValue(true);
            }
        }
    }
}