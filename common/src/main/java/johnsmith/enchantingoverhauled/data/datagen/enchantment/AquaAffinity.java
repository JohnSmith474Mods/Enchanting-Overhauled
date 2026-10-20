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

public class AquaAffinity {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) ->Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.HEAD_ARMOR_ENCHANTABLE),
                    2,
                    AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
                    Enchantment.constantCost(1),
                    Enchantment.constantCost(41),
                    4,
                    EquipmentSlotGroup.HEAD
            );

    public static final EnchantmentAttributeEffect EFFECT = new EnchantmentAttributeEffect(
            ResourceLocation.withDefaultNamespace("enchantment.aqua_affinity"),
            Attributes.SUBMERGED_MINING_SPEED,
            new PointerValue("aqua_affinity.submerged_mining_speed", new DiminishingReturnsValue(5F/3F, 1F/3F, 1.F)),
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(AquaAffinity.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.ATTRIBUTES, AquaAffinity.EFFECT)
                    .build(AdaptedEnchantments.AQUA_AFFINITY.location());
}
