package johnsmith.enchantingoverhauled.mixin.enchantment;

import johnsmith.enchantingoverhauled.config.Config;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DigDurabilityEnchantment.class)
public class UnbreakingEnchantmentMixin {
    /**
     * Modifies the 0.6F (60%) chance for Unbreaking to *fail* on armor.
     * This is replaced by Config.UNBREAKING_ARMOR_PENALTY_FACTOR.
     *
     * @param originalValue The original 0.6F constant.
     * @return The new value from the config, or the original if the config is not set (<= 0).
     */
    @ModifyConstant(method = "shouldIgnoreDurabilityDrop(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/util/RandomSource;)Z",
            constant = @Constant(floatValue = 0.6F))
    private static float modifyArmorPenalty(float originalValue) {
        // Use the established pattern: if the config value is 0 or less,
        // use the original vanilla value (0.6F).
        if (Config.UNBREAKING_ARMOR_PENALTY_FACTOR < 0.0F) {
            return originalValue;
        } else {
            // Otherwise, use the value from the config.
            return Config.UNBREAKING_ARMOR_PENALTY_FACTOR.floatValue();
        }
    }

    /**
     * Modifies the 'level' parameter used in the unbreaking calculation.
     * This intercepts the argument 'bound' (which is level + 1) being passed
     * to random.nextInt() and recalculates it using the original 'level'
     * and the config multiplier.
     *
     * @param level The original level of the enchantment.
     * @return The new bound for random.nextInt(), e.g., (level * STRENGTH) + 1.
     */
    @ModifyArg(method = "shouldIgnoreDurabilityDrop(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/util/RandomSource;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"),
            index = 0)
    private static int modifyLevelForNextInt(int level) {
        return level * Config.UNBREAKING_STRENGTH;
    }
}
