package johnsmith.enchantingoverhauled.mixin.client.item;

import johnsmith.enchantingoverhauled.api.enchantment.effect.BowChargeTimeEffect;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(ItemProperties.class)
public class ItemPropertiesMixin {

    /**
     * Intercepts the registration of Item Properties.
     * If we detect the "pull" property for the Bow, we replace the logic with our own
     * that accounts for the Quick Charge enchantment.
     */
    @ModifyVariable(
            method = "register(Lnet/minecraft/world/item/Item;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/item/ClampedItemPropertyFunction;)V",
            at = @At("HEAD"),
            index = 2, // The 3rd argument is the 'property' function
            argsOnly = true
    )
    private static ClampedItemPropertyFunction overrideBowPullProperty(ClampedItemPropertyFunction original, Item item, ResourceLocation name) {
        // Check if this is the specific registration we want to override
        if (item == Items.BOW && name.equals(ResourceLocation.withDefaultNamespace("pull"))) {

            // Return our custom logic
            return (stack, level, entity, seed) -> {
                if (entity == null) {
                    return 0.0F;
                }
                if (entity.getUseItem() != stack) {
                    return 0.0F;
                }

                // 1. Calculate actual ticks used so far
                // (useDuration - remaining) = ticks held
                int chargeTicks = stack.getUseDuration(entity) - entity.getUseItemRemainingTicks();

                // 2. Calculate Multiplier (Exact same logic as BowItemMixin)
                float baseTime = 20.0F;
                float reductionSeconds = 0.0F;

                // Safe enchantment lookup
                ItemEnchantments enchantments = stack.getEnchantments();
                for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
                    BowChargeTimeEffect effect = entry.getKey().value().effects().get(EnchantmentEffectComponentRegistry.BOW_CHARGE_TIME);
                    if (effect != null) {
                        reductionSeconds += effect.amount().calculate(entry.getValue());
                    }
                }

                // 3. Determine new max charge
                float newMaxCharge = baseTime + (reductionSeconds * 20.0F);
                newMaxCharge = Math.max(0.1F, newMaxCharge); // Prevent div/0

                // 4. Calculate how "full" the pull is relative to the NEW max charge
                // Vanilla uses: chargeTicks / 20.0F
                // We use:       chargeTicks / newMaxCharge
                return (float) (chargeTicks / newMaxCharge);
            };
        }

        // For all other items/properties, keep the original
        return original;
    }
}