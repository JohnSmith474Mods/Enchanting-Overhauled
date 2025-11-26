package johnsmith.enchantingoverhauled.mixin.server.level;

import johnsmith.enchantingoverhauled.api.player.SavedItemEntry;
import johnsmith.enchantingoverhauled.api.player.SavedItemsAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void restoreBoundItems(ServerPlayer oldPlayer, boolean keptInventory, CallbackInfo ci) {
        // Retrieve items saved on the old player entity
        List<SavedItemEntry> savedItems = ((SavedItemsAccessor) oldPlayer).enchanting_overhauled$getSavedItems();

        if (savedItems != null && !savedItems.isEmpty()) {
            ServerPlayer self = (ServerPlayer) (Object) this;

            for (SavedItemEntry entry : savedItems) {
                // Try to put it back in the exact same slot (e.g., Armor slots 36-39)
                // "setItem" handles mapping the index to the correct compartment.
                self.getInventory().setItem(entry.slot(), entry.stack());
            }
        }
    }
}