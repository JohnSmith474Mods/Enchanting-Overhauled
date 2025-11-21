package johnsmith.enchantingoverhauled.mixin.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.WaterWalkerEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterWalkerEnchantment.class)
public class DepthStriderEnchantmentMixin {

    @Inject(method = "checkCompatibility", at = @At("HEAD"), cancellable = true)
    public void checkCompatibility(Enchantment other, CallbackInfoReturnable<Boolean> cir) {
        // Return true unless the other enchantment is also Depth Strider.
        // This overrides the vanilla check that prevents stacking with Frost Walker.
        cir.setReturnValue(!(other instanceof WaterWalkerEnchantment));
    }
}