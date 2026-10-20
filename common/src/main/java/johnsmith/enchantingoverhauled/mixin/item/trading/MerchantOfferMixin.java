package johnsmith.enchantingoverhauled.mixin.item.trading;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.item.Items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantOffer.class)
public abstract class MerchantOfferMixin {
    @Shadow public abstract ItemStack getResult();

    @Inject(method = "resetUses", at = @At("HEAD"), cancellable = true)
    private void enchanting_overhauled$preventRestock(CallbackInfo ci) {
        ItemStack result = this.getResult();
        if (result.is(Items.ENCHANTED_TOME.get()) && !Config.ALLOW_ENCHANTED_TOME_RESTOCK.get()) {
            ci.cancel();
        } else if (result.is(Items.ENCHANTED_PAGE.get()) && !Config.ALLOW_ENCHANTED_PAGE_RESTOCK.get()) {
            ci.cancel();
        }
    }
}