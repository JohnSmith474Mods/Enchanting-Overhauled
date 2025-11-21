package johnsmith.enchantingoverhauled.mixin.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.MultiShotEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiShotEnchantment.class)
public class MultishotEnchantmentMixin {

    @Inject(method = "checkCompatibility", at = @At("HEAD"), cancellable = true)
    public void checkCompatibility(Enchantment other, CallbackInfoReturnable<Boolean> cir) {
        // Return true unless the other enchantment is also Multishot.
        // This overrides the vanilla check that prevents stacking with Piercing.
        cir.setReturnValue(!(other instanceof MultiShotEnchantment));
    }
}