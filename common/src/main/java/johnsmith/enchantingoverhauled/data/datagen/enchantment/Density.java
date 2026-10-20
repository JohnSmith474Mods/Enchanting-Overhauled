package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.AddValue;

import java.util.function.Function;

public class Density {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.MACE_ENCHANTABLE),
            5,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(5, 8),
            Enchantment.dynamicCost(25, 8),
            2,
            EquipmentSlotGroup.MAINHAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Density.DEFINITION.apply(context))
                    .exclusiveWith(context.lookup(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                    .withEffect(EnchantmentEffectComponents.SMASH_DAMAGE_PER_FALLEN_BLOCK,
                            new AddValue(new PointerValue("density.smash_damage_per_fallen_bock",new DiminishingReturnsValue(0.8F, 0.2F, 0.2F))))
                    .build(AdaptedEnchantments.DENSITY.location());
}