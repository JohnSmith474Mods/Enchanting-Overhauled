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

public class Respiration {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.HEAD_ARMOR_ENCHANTABLE),
            2,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(10, 10),
            Enchantment.dynamicCost(40, 10),
            4,
            EquipmentSlotGroup.HEAD
    );

    public static final EnchantmentAttributeEffect EFFECT = new EnchantmentAttributeEffect(
            ResourceLocation.withDefaultNamespace("enchantment.respiration"),
            Attributes.OXYGEN_BONUS,
            new PointerValue("respiration.oxygen_bonus", new DiminishingReturnsValue(1.5F, 0.5F, 0.5F)),
            AttributeModifier.Operation.ADD_VALUE
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Respiration.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.ATTRIBUTES, Respiration.EFFECT)
                    .build(AdaptedEnchantments.RESPIRATION.location());
}