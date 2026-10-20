package johnsmith.enchantingoverhauled.mixin.menu;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipe;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.tags.EnchantmentTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(GrindstoneMenu.class)
public class GrindstoneMenuMixin {

    @Shadow @Final private Container resultSlots;
    @Shadow @Final Container repairSlots;

    @Unique
    private Player enchanting_overhauled$player;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void enchanting_overhauled$capturePlayer(int id, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        this.enchanting_overhauled$player = inventory.player;
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void enchanting_overhauled$onGrindstoneCreateResult(CallbackInfo ci) {
        ItemStack result = this.resultSlots.getItem(0);

        if (!result.isEmpty() && this.enchanting_overhauled$player != null) {
            ItemStack input0 = this.repairSlots.getItem(0);
            ItemStack input1 = this.repairSlots.getItem(1);

            // Detect if the inputs are custom arbitrary items using STORED_ENCHANTMENTS
            boolean customStored = (input0.has(DataComponents.STORED_ENCHANTMENTS) && !input0.is(Items.ENCHANTED_BOOK)) ||
                    (input1.has(DataComponents.STORED_ENCHANTMENTS) && !input1.is(Items.ENCHANTED_BOOK));

            Optional<EnchantRecipe> recipeOpt = EnchantRecipe.reverse(this.enchanting_overhauled$player.level().registryAccess(), result);

            // Only interfere if it is a custom item or possesses a reverse transmuting recipe
            if (customStored || recipeOpt.isPresent()) {
                ItemEnchantments.Mutable curses = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

                // Extract curses manually so they aren't destroyed during the wipe
                if (!input0.isEmpty()) {
                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentLib.getEnchantments(input0).entrySet()) {
                        if (entry.getKey().is(EnchantmentTags.CURSE)) {
                            curses.set(entry.getKey(), Math.max(curses.getLevel(entry.getKey()), entry.getIntValue()));
                        }
                    }
                }
                if (!input1.isEmpty()) {
                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentLib.getEnchantments(input1).entrySet()) {
                        if (entry.getKey().is(EnchantmentTags.CURSE)) {
                            curses.set(entry.getKey(), Math.max(curses.getLevel(entry.getKey()), entry.getIntValue()));
                        }
                    }
                }

                ItemStack newResult = result;

                // Transmute if a reverse recipe exists
                if (recipeOpt.isPresent()) {
                    Optional<Holder<Item>> firstInput = recipeOpt.get().input().stream().findFirst();
                    if (firstInput.isPresent()) {
                        newResult = result.transmuteCopy(firstInput.get().value(), result.getCount());
                    }
                }

                // Strip ALL remnant enchantment components safely
                newResult.remove(DataComponents.STORED_ENCHANTMENTS);
                newResult.remove(DataComponents.ENCHANTMENTS);

                // Re-apply preserved curses to the ENCHANTMENTS component (safe for paper/books)
                if (!curses.toImmutable().isEmpty()) {
                    newResult.set(DataComponents.ENCHANTMENTS, curses.toImmutable());
                }

                this.resultSlots.setItem(0, newResult);

                // Force a broadcast to synchronize the UI slot for the client
                ((GrindstoneMenu) (Object) this).broadcastChanges();
            }
        }
    }
}