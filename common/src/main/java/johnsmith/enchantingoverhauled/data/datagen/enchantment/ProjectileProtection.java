package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;

import java.util.function.Function;

public class ProjectileProtection {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.CHEST_ARMOR),
            5,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(3, 6),
            Enchantment.dynamicCost(9, 6),
            2,
            EquipmentSlotGroup.CHEST
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(ProjectileProtection.DEFINITION.apply(context))
                    .withEffect(
                            EnchantmentEffectComponents.DAMAGE_PROTECTION,
                            new AddValue(new PointerValue("projectile_protection.damage_protection", new DiminishingReturnsValue(6.F, 2.F, 2.F))),
                            DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)).tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY)))
                    )
                    .build(AdaptedEnchantments.PROJECTILE_PROTECTION.location());
}