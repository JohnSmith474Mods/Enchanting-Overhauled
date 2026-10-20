package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantingoverhauled.api.tag.Tags;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.ApplyMobEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.function.Function;

public class BaneOfArthropods {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(Tags.ALL_MELEE),
            5,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(5, 8),
            Enchantment.dynamicCost(25, 8),
            2,
            EquipmentSlotGroup.MAINHAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(BaneOfArthropods.DEFINITION.apply(context))
                    .withEffect(
                            EnchantmentEffectComponents.DAMAGE,
                            new AddValue(new PointerValue("bane_of_arthropods.damage", new DiminishingReturnsValue(4.5F, 1.5F, 1.5F))),
                            LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)))
                    )
                    .withEffect(
                            EnchantmentEffectComponents.POST_ATTACK,
                            EnchantmentTarget.ATTACKER,
                            EnchantmentTarget.VICTIM,
                            new ApplyMobEffect(
                                    HolderSet.direct(MobEffects.MOVEMENT_SLOWDOWN),
                                    new PointerValue("bane_of_arthropods.slowness.min_duration", new DiminishingReturnsValue(1.5F, 0.5F, 0.5F)),
                                    new PointerValue("bane_of_arthropods.slowness.max_duration", new DiminishingReturnsValue(6.0F, 2.0F, 2.0F)),
                                    new PointerValue("bane_of_arthropods.slowness.min_amplifier", LevelBasedValue.constant(0.0F)),
                                    new PointerValue("bane_of_arthropods.slowness.max_amplifier", LevelBasedValue.perLevel(1.0F))
                            ),
                            LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)))
                                    .and(DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true)))
                    )
                    .build(AdaptedEnchantments.BANE_OF_ARTHROPODS.location());
}