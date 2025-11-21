package johnsmith.enchantingoverhauled.mixin.enchantment;

import net.minecraft.world.item.enchantment.ArrowPiercingEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArrowPiercingEnchantment.class)
public class PiercingEnchantmentMixin {

    @Inject(method = "checkCompatibility", at = @At("HEAD"), cancellable = true)
    public void checkCompatibility(Enchantment other, CallbackInfoReturnable<Boolean> cir) {
        // Return true unless the other enchantment is also Piercing.
        // This overrides the vanilla check that prevents stacking with Multishot.
        cir.setReturnValue(!(other instanceof ArrowPiercingEnchantment));
    }
}