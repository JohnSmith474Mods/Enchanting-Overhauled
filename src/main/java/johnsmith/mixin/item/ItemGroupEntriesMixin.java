package johnsmith.mixin.item;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to the ItemGroup.Entries interface.
 * This intercepts the default 'add(ItemConvertible)' method to prevent
 * the enchanting table from being added to any creative tab.
 */
@Mixin(ItemGroup.Entries.class)
public interface ItemGroupEntriesMixin {

    /**
     * Injects at the head of the default 'add(ItemConvertible)' method.
     * Checks if the item being added is the enchanting table.
     * If it is, the method is canceled, preventing it from being added.
     *
     * @param item The item being added.
     * @param ci   CallbackInfo to cancel the method.
     */
    @Inject(method = "add(Lnet/minecraft/item/ItemConvertible;)V",
                at = @At("HEAD"),
       cancellable = true)
    // The method must not be private when injecting into an interface
    private void preventEnchantingTableAdd(ItemConvertible item, CallbackInfo ci) {
        if (item.asItem() == Items.ENCHANTING_TABLE) {
            // Cancel the 'add' method
            ci.cancel();
        }
    }
}