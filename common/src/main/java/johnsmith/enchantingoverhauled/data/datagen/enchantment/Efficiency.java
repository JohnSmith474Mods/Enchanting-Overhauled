package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;

import java.util.function.Function;

public class Efficiency {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.MINING_ENCHANTABLE),
            10,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(1, 10),
            Enchantment.dynamicCost(51, 10),
            1,
            EquipmentSlotGroup.MAINHAND
    );

    public static final EnchantmentAttributeEffect EFFECT = new EnchantmentAttributeEffect(
            ResourceLocation.withDefaultNamespace("enchantment.efficiency"),
            Attributes.MINING_EFFICIENCY,
            new PointerValue("efficiency.mining_efficiency", new DiminishingReturnsValue(22.F, 4.F, 0.F)),
            AttributeModifier.Operation.ADD_VALUE
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Efficiency.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.ATTRIBUTES, Efficiency.EFFECT)
                    .build(AdaptedEnchantments.EFFICIENCY.location());
}