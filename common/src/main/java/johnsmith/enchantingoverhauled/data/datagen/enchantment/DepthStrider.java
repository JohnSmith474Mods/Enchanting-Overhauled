package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;

import java.util.function.Function;

public class DepthStrider {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
            2,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(10, 10),
            Enchantment.dynamicCost(25, 10),
            4,
            EquipmentSlotGroup.FEET
    );

    public static final EnchantmentAttributeEffect EFFECT = new EnchantmentAttributeEffect(
            ResourceLocation.withDefaultNamespace("enchantment.depth_strider"),
            Attributes.WATER_MOVEMENT_EFFICIENCY,
            new PointerValue("depth_strider.water_movement_efficiency",new DiminishingReturnsValue(0.4375F, 0.125F, 0.25F)),
            AttributeModifier.Operation.ADD_VALUE
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(DepthStrider.DEFINITION.apply(context))
                    .exclusiveWith(context.lookup(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.BOOTS_EXCLUSIVE))
                    .withEffect(EnchantmentEffectComponents.ATTRIBUTES, DepthStrider.EFFECT)
                    .build(AdaptedEnchantments.DEPTH_STRIDER.location());
}