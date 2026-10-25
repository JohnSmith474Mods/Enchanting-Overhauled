package johnsmith.enchantingoverhauled.mixin.item;

import johnsmith.enchantingoverhauled.api.item.ArcaneItemNamingUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into {@link Item} to apply dynamic {@link ArcaneItemNamingUtil} theme and magic school
 * naming to vanilla {@link Items#ENCHANTED_BOOK} stacks.
 * <p>
 * Note: In Minecraft 1.21.2+, {@code EnchantedBookItem} was removed and {@link Items#ENCHANTED_BOOK}
 * is instantiated directly as an {@link Item}.
 */
@Mixin(Item.class)
public abstract class EnchantedBookItemMixin {

    @Inject(
            method = "getName(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void enchanting_overhauled$formatEnchantedBookName(ItemStack stack, CallbackInfoReturnable<Component> cir) {
        if (stack != null && stack.is(Items.ENCHANTED_BOOK)) {
            Component formatted = ArcaneItemNamingUtil.formatItemName(stack, cir.getReturnValue());
            cir.setReturnValue(formatted);
        }
    }
}