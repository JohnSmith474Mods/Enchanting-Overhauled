package johnsmith.enchantingoverhauled.mixin.item;

import com.llamalad7.mixinextras.sugar.Local;
import johnsmith.enchantingoverhauled.api.enchantment.effect.BowChargeTimeEffect;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

@Mixin(BowItem.class)
public class BowItemMixin {

    /**
     * Intercepts the 'charge' argument passed to 'getPowerForTime'.
     * This allows us to fake a higher charge time (Quick Charge) without messing with local variables.
     */
    @ModifyArg(
            method = "releaseUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"
            ),
            index = 0
    )
    private int modifyBowChargeArgument(int chargeTicks, @Local(argsOnly = true) ItemStack stack) {
        // 1. Calculate base time (20 ticks = 1.0s)
        float baseTime = 20.0F;
        float reductionSeconds = 0.0F;

        // 2. Check for custom Bow Charge Time component
        // Note: Use stack.getEnchantments() for 1.21+ standard compliance
        ItemEnchantments enchantments = stack.getEnchantments();

        for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
            BowChargeTimeEffect effect = entry.getKey().value().effects().get(EnchantmentEffectComponentRegistry.BOW_CHARGE_TIME);

            if (effect != null) {
                int lvl = entry.getValue();
                // Add up reductions (e.g., -0.25s)
                reductionSeconds += effect.amount().calculate(lvl);
            }
        }

        // If no reduction, return original
        if (reductionSeconds == 0.0F) {
            return chargeTicks;
        }

        // 3. Calculate new "Max Charge Time" needed
        // Example: -0.5s reduction means we need 10 ticks instead of 20.
        // newMax = 20 + (-0.5 * 20) = 10 ticks.
        float newMaxCharge = baseTime + (reductionSeconds * 20.0F);

        // Clamp to a reasonable minimum (e.g., 1 tick)
        newMaxCharge = Math.max(0.1F, newMaxCharge);

        // 4. Calculate the Multiplier
        // If we need 10 ticks for full power, but vanilla expects 20...
        // We must multiply our actual ticks by (20 / 10) = 2x.
        float multiplier = baseTime / newMaxCharge;

        return (int) (chargeTicks * multiplier);
    }
}