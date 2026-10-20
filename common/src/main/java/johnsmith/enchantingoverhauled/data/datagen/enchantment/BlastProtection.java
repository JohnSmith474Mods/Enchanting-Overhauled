package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
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
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;

import java.util.function.Function;

public class BlastProtection {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.CHEST_ARMOR),
            2,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(5, 8),
            Enchantment.dynamicCost(13, 8),
            4,
            EquipmentSlotGroup.CHEST
    );

    public static final EnchantmentAttributeEffect EFFECT = new EnchantmentAttributeEffect(
            ResourceLocation.withDefaultNamespace("enchantment.blast_protection"),
            Attributes.EXPLOSION_KNOCKBACK_RESISTANCE,
            new PointerValue("blast_protection.knockback_resistance", new LevelBasedValue.Fraction(
                    new DiminishingReturnsValue(4.F, 1.F, 0.F),
                    LevelBasedValue.constant(10.F)
            )),
            AttributeModifier.Operation.ADD_VALUE
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(BlastProtection.DEFINITION.apply(context))
                    .withEffect(
                            EnchantmentEffectComponents.DAMAGE_PROTECTION,
                            new AddValue(new PointerValue("blast_protection.damage_protection", new DiminishingReturnsValue(6.F, 2.F, 2.F))),
                            DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_EXPLOSION)).tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY)))
                    )
                    .withEffect(EnchantmentEffectComponents.ATTRIBUTES, BlastProtection.EFFECT)
                    .build(AdaptedEnchantments.BLAST_PROTECTION.location());
}