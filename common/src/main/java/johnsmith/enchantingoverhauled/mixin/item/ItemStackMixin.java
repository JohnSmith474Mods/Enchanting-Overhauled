package johnsmith.enchantingoverhauled.mixin.item;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {

    @Shadow
    public abstract Item getItem();

    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void enchanting_overhauled$modifyIsEnchantable(CallbackInfoReturnable<Boolean> cir) {
        if (!this.has(DataComponents.ENCHANTABLE)) {
            cir.setReturnValue(false);
            return;
        }
        Object itemEnchantmentsComponent = this.get(DataComponents.ENCHANTMENTS);
        cir.setReturnValue(itemEnchantmentsComponent != null);
    }

    @Inject(method = "isEnchanted", at = @At("HEAD"), cancellable = true)
    private void enchanting_overhauled$checkStoredEnchantments(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;
        ItemEnchantments stored = self.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored != null && !stored.isEmpty()) {
            cir.setReturnValue(true);
        }
    }
}