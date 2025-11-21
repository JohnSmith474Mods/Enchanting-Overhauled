package johnsmith.enchantingoverhauled.mixin.enchantment;

import net.minecraft.world.item.enchantment.ArrowInfiniteEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArrowInfiniteEnchantment.class)
public class InfinityEnchantmentMixin {

    @Inject(method = "checkCompatibility", at = @At("HEAD"), cancellable = true)
    public void checkCompatibility(Enchantment other, CallbackInfoReturnable<Boolean> cir) {
        // Return true unless the other enchantment is also Infinity.
        // This overrides the vanilla check that prevents stacking with Mending.
        cir.setReturnValue(!(other instanceof ArrowInfiniteEnchantment));
    }
}