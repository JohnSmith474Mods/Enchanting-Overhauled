package johnsmith.enchantingoverhauled.mixin.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FrostWalkerEnchantment.class)
public class FrostWalkerEnchantmentMixin {

    @Inject(method = "checkCompatibility", at = @At("HEAD"), cancellable = true)
    public void checkCompatibility(Enchantment other, CallbackInfoReturnable<Boolean> cir) {
        // Return true unless the other enchantment is also Frost Walker.
        // This overrides the vanilla check that prevents stacking with Depth Strider.
        cir.setReturnValue(!(other instanceof FrostWalkerEnchantment));
    }
}