package johnsmith.enchantingoverhauled.mixin.enchantment;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import johnsmith.enchantingoverhauled.config.Config;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    /**
     * Overrides the maximum level for all scalable enchantments based on the global configuration.
     * <p>
     * Logic:
     * 1. If the enchantment is naturally single-level (e.g. Mending, Silk Touch), preserve it.
     * 2. Otherwise, force it to the configured global max level (e.g., 3).
     */
    @ModifyReturnValue(method = "getMaxLevel", at = @At("RETURN"))
    public int getMaxLevel(final int original) {
        if (original == 1) {
            return original;
        }

        int newLevel = Config.BOUNDED_ENCHANTMENT_MAX_LEVEL.get();

        if (newLevel > 0) {
            return newLevel;
        }

        return original;
    }
}
