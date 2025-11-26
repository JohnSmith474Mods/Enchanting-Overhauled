package johnsmith.enchantingoverhauled.mixin.enchantment;

import johnsmith.enchantingoverhauled.config.Config;

import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @Shadow
    @Final
    private Enchantment.EnchantmentDefinition definition;

    /**
     * Overrides the maximum level for all scalable enchantments based on the global configuration.
     * <p>
     * Logic:
     * 1. If the enchantment is naturally single-level (e.g. Mending, Silk Touch), preserve it.
     * 2. Otherwise, force it to the configured global max level (e.g., 3).
     */
    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    public void getMaxLevel(CallbackInfoReturnable<Integer> cir) {
        int originalMax = this.definition.maxLevel();
        if (originalMax == 1) { return; }
        cir.setReturnValue(Config.BOUNDED_ENCHANTMENT_MAX_LEVEL.get());
    }
}
