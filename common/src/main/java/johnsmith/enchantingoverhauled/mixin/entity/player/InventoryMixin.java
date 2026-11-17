package johnsmith.enchantingoverhauled.mixin.entity.player;

import johnsmith.enchantingoverhauled.api.enchantment.effect.BindingCurseEffect;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.effect.VanishingCurseEffect;
import johnsmith.enchantingoverhauled.api.player.SavedItemEntry;
import johnsmith.enchantingoverhauled.api.player.SavedItemsAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow @Final public Player player;
    @Shadow public abstract ItemStack getItem(int index);
    @Shadow public abstract void setItem(int index, ItemStack stack);
    @Shadow public abstract int getContainerSize();

    /**
     * PHASE 1: Binding Curse
     * Runs BEFORE drops occur. Scans inventory for bound items, saves them, and removes them.
     */
    @Inject(method = "dropAll", at = @At("HEAD"))
    private void scanAndSaveBoundItems(CallbackInfo ci) {
        for (int i = 0; i < this.getContainerSize(); i++) {
            // CONSTRAINT: Only check Armor Slots (Indices 36, 37, 38, 39)
            // 0-35 = Main Inventory + Hotbar
            // 36-39 = Boots, Leggings, Chestplate, Helmet
            // 40 = Offhand
            if (i < 36 || i > 39) {
                continue;
            }

            ItemStack stack = this.getItem(i);
            if (stack.isEmpty()) continue;

            ItemEnchantments enchantments = stack.getEnchantments();
            for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
                // Check for Binding Curse Effect
                BindingCurseEffect bindingEffect = entry.getKey().value().effects().get(EnchantmentEffectComponentRegistry.BINDING_CHANCE);

                if (bindingEffect != null) {
                    int level = entry.getValue();
                    float chance = bindingEffect.chance().calculate(level);

                    // Roll for "Keep Item"
                    if (this.player.getRandom().nextFloat() < chance) {
                        // Success: Save the item with its SLOT index so it returns to the correct body part
                        ((SavedItemsAccessor) this.player).enchanting_overhauled$addSavedItem(new SavedItemEntry(i, stack.copy()));

                        // Remove from inventory so it isn't dropped by the subsequent vanilla logic
                        this.setItem(i, ItemStack.EMPTY);
                        break; // Item handled, move to next slot
                    }
                }
            }
        }
    }
}