package johnsmith.enchantingoverhauled.client.model.property;

import com.mojang.serialization.MapCodec;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.effect.BowChargeTimeEffect;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public record BowPullProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<BowPullProperty> CODEC = MapCodec.unit(new BowPullProperty());

    @Override
    public float get(@NotNull ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
        if (itemOwner == null || itemOwner.asLivingEntity().getUseItem() != itemStack) {
            return 0.0F;
        }

        // 1. Calculate actual ticks used so far
        int chargeTicks = itemStack.getUseDuration(itemOwner.asLivingEntity()) - itemOwner.asLivingEntity().getUseItemRemainingTicks();

        // 2. Calculate Multiplier (Your Custom Logic)
        float baseTime = 20.0F;
        float reductionSeconds = 0.0F;
        ItemEnchantments enchantments = itemStack.getEnchantments();
        for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
            // Assuming .effects() is available via your API/Mixin
            BowChargeTimeEffect effect = entry.getKey().value().effects().get(EnchantmentEffectComponentRegistry.BOW_CHARGE_TIME);
            if (effect != null) {
                reductionSeconds += effect.amount().calculate(entry.getValue());
            }
        }

        // 3. Determine new max charge
        float newMaxCharge = baseTime + (reductionSeconds * 20.0F);
        newMaxCharge = Math.max(0.01F, newMaxCharge);

        // 4. Calculate progress
        return (float) (chargeTicks / newMaxCharge);
    }

    @Override
    public @NotNull MapCodec<? extends RangeSelectItemModelProperty> type() {
        return CODEC;
    }
}