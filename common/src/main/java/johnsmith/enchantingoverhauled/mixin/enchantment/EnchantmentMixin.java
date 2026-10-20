package johnsmith.enchantingoverhauled.mixin.enchantment;

import johnsmith.enchantingoverhauled.Config;
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

        if (Config.NORMALIZE_ENCHANTMENT_MAX_LEVEL.get()) {
            cir.setReturnValue(Config.ENCHANTMENT_MAX_LEVEL.get());
        }
    }

    /**
     * Clamps the minimum enchanting cost using the configured global lower and upper bounds.
     */
    @Inject(method = "getMinCost", at = @At("RETURN"), cancellable = true)
    public void getMinCost(int level, CallbackInfoReturnable<Integer> cir) {
        int minBound = Config.GLOBAL_ENCHANTMENT_MIN_COST.get();
        int maxBound = Math.max(minBound, Config.GLOBAL_ENCHANTMENT_MAX_COST.get());
        cir.setReturnValue(Math.clamp(cir.getReturnValue(), minBound, maxBound));
    }

    /**
     * Clamps the maximum enchanting cost using the configured global lower and upper bounds.
     */
    @Inject(method = "getMaxCost", at = @At("RETURN"), cancellable = true)
    public void getMaxCost(int level, CallbackInfoReturnable<Integer> cir) {
        int minBound = Config.GLOBAL_ENCHANTMENT_MIN_COST.get();
        int maxBound = Math.max(minBound, Config.GLOBAL_ENCHANTMENT_MAX_COST.get());
        cir.setReturnValue(Math.clamp(cir.getReturnValue(), minBound, maxBound));
    }
}