package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.NegateValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;

import java.util.function.Function;

public class FireProtection {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.CHEST_ARMOR),
            5,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(10, 8),
            Enchantment.dynamicCost(18, 8),
            2,
            EquipmentSlotGroup.CHEST
    );

    public static final EnchantmentAttributeEffect EFFECT = new EnchantmentAttributeEffect(
            ResourceLocation.withDefaultNamespace("enchantment.fire_protection"),
            Attributes.BURNING_TIME,
            new PointerValue("fire_protection.burning_time", new NegateValue(
                    new LevelBasedValue.Fraction(
                            new DiminishingReturnsValue(4.F, 1.F, 0.F),
                            LevelBasedValue.constant(10.F)
                    )
            )),
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(FireProtection.DEFINITION.apply(context))
                    .withEffect(
                            EnchantmentEffectComponents.DAMAGE_PROTECTION,
                            new AddValue(new PointerValue("fire_protection.damage_protection", new DiminishingReturnsValue(6.F, 2.F, 2.F))),
                            AllOfCondition.allOf(
                                    DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_FIRE)).tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY)))
                            )
                    )
                    .withEffect(EnchantmentEffectComponents.ATTRIBUTES, FireProtection.EFFECT)
                    .build(AdaptedEnchantments.FIRE_PROTECTION.location());
}