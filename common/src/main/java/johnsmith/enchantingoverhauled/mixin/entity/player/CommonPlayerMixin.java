package johnsmith.enchantingoverhauled.mixin.entity.player;

import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.effect.VanishingCurseEffect;
import johnsmith.enchantingoverhauled.api.player.SavedItemEntry;
import johnsmith.enchantingoverhauled.api.player.SavedItemsAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(Player.class)
public class CommonPlayerMixin implements SavedItemsAccessor {

    @Unique
    private final List<SavedItemEntry> enchanting_overhauled$savedItems = new ArrayList<>();

    @Override
    public List<SavedItemEntry> enchanting_overhauled$getSavedItems() {
        return this.enchanting_overhauled$savedItems;
    }

    @Override
    public void enchanting_overhauled$addSavedItem(SavedItemEntry entry) {
        this.enchanting_overhauled$savedItems.add(entry);
    }

    /**
     * Redirects the check inside destroyVanishingCursedItems().
     * If we return FALSE, the item is NOT destroyed and will be dropped normally later.
     */
    @Redirect(
            method = "destroyVanishingCursedItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;has(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/component/DataComponentType;)Z"
            )
    )
    private boolean interceptVanishingDestruction(ItemStack stack, DataComponentType<?> component) {
        // 1. Verify we are checking for the right component
        if (component != EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) {
            return EnchantmentHelper.has(stack, component);
        }

        // 2. Check Vanilla Logic. If it doesn't have the curse, it's safe.
        boolean hasVanillaCurse = EnchantmentHelper.has(stack, component);
        if (!hasVanillaCurse) {
            return false;
        }

        // 3. Check for Custom Probability
        Player self = (Player) (Object) this;
        ItemEnchantments enchantments = stack.getEnchantments();

        for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
            VanishingCurseEffect effect = entry.getKey().value().effects().get(EnchantmentEffectComponentRegistry.VANISHING_CHANCE);

            if (effect != null) {
                int level = entry.getValue();
                float chance = effect.chance().calculate(level);

                // Roll RNG:
                // < chance : Vanish (Return TRUE -> vanilla destroys it)
                // > chance : Survive (Return FALSE -> vanilla skips destruction)
                return self.getRandom().nextFloat() < chance;
            }
        }

        // Fallback: Vanilla behavior (Vanish 100%)
        return true;
    }
}