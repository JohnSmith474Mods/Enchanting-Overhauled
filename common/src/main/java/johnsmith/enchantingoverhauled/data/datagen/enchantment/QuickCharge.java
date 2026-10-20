package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantingoverhauled.api.tag.Tags;

import johnsmith.enchantmentcore.api.enchantment.effect.BowChargeTimeEffect;
import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.NegateValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;
import johnsmith.enchantmentcore.api.registry.EnchantmentEffectComponents;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.AddValue;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class QuickCharge {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(Tags.ALL_RANGED),
            5,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(12, 20),
            Enchantment.constantCost(50),
            2,
            EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(QuickCharge.DEFINITION.apply(context))
                    .withSpecialEffect(
                            EnchantmentEffectComponents.BOW_CHARGE_TIME,
                            new BowChargeTimeEffect(new NegateValue(new PointerValue("quick_charge.bow_charge_time", new DiminishingReturnsValue(0.32F, 0.08F, 0.0F))))
                    )
                    .withSpecialEffect(
                            net.minecraft.world.item.enchantment.EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME,
                            new AddValue(new NegateValue(new PointerValue("quick_charge.crossbow_charge_time", new DiminishingReturnsValue(0.4F, 0.1F, 0.0F))))
                    )
                    .withSpecialEffect(
                            net.minecraft.world.item.enchantment.EnchantmentEffectComponents.CROSSBOW_CHARGING_SOUNDS,
                            List.of(
                                    new CrossbowItem.ChargingSounds(Optional.of(SoundEvents.CROSSBOW_QUICK_CHARGE_1), Optional.empty(), Optional.of(SoundEvents.CROSSBOW_LOADING_END)),
                                    new CrossbowItem.ChargingSounds(Optional.of(SoundEvents.CROSSBOW_QUICK_CHARGE_2), Optional.empty(), Optional.of(SoundEvents.CROSSBOW_LOADING_END)),
                                    new CrossbowItem.ChargingSounds(Optional.of(SoundEvents.CROSSBOW_QUICK_CHARGE_3), Optional.empty(), Optional.of(SoundEvents.CROSSBOW_LOADING_END))
                            )
                    )
                    .build(AdaptedEnchantments.QUICK_CHARGE.location());
}