package johnsmith.enchantingoverhauled.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {
    @ModifyReturnValue(method = "canEntityWalkOnPowderSnow", at = @At("RETURN"))
    private static boolean canEntityWalkOnPowderSnow(final boolean original, @Local(argsOnly = true) Entity entity) {
        if (!original && entity instanceof LivingEntity living) {
            ItemStack stack = living.getItemBySlot(EquipmentSlot.FEET);

            if (stack.isEmpty()) {
                return false;
            }

            return EnchantmentHelper.has(stack, EnchantmentEffectComponentRegistry.POWDER_SNOW_WALKABLE);
        }

        return original;
    }
}