package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.AddValue;

import java.util.List;
import java.util.function.Function;

public class Riptide {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.TRIDENT_ENCHANTABLE),
            2,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(17, 7),
            Enchantment.constantCost(50),
            4,
            EquipmentSlotGroup.HAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Riptide.DEFINITION.apply(context))
                    .exclusiveWith(context.lookup(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.RIPTIDE_EXCLUSIVE))
                    .withSpecialEffect(
                            EnchantmentEffectComponents.TRIDENT_SPIN_ATTACK_STRENGTH,
                            new AddValue(new PointerValue("riptide.trident_spin_attack_strength", new DiminishingReturnsValue(1.5F, 0.5F, 0.5F)))
                    )
                    .withSpecialEffect(
                            EnchantmentEffectComponents.TRIDENT_SOUND,
                            List.of(SoundEvents.TRIDENT_RIPTIDE_1, SoundEvents.TRIDENT_RIPTIDE_2, SoundEvents.TRIDENT_RIPTIDE_3)
                    )
                    .build(AdaptedEnchantments.RIPTIDE.location());
}