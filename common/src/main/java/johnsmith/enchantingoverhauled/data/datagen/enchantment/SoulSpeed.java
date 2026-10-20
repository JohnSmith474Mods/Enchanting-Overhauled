package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.advancements.critereon.*;
import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.DamageItem;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.PlaySoundEffect;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.EnchantmentLevelProvider;

import java.util.function.Function;

public class SoulSpeed {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
            1,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(10, 10),
            Enchantment.dynamicCost(25, 10),
            8,
            EquipmentSlotGroup.FEET
    );

    public static final EntityPredicate.Builder TICK_PREDICATE = EntityPredicate.Builder.entity()
            .periodicTick(5)
            .flags(EntityFlagsPredicate.Builder.flags().setIsFlying(false).setOnGround(true))
            .moving(MovementPredicate.horizontalSpeed(Doubles.atLeast(1.0E-5F)))
            .movementAffectedBy(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.SOUL_SPEED_BLOCKS)));

    public static final EnchantmentAttributeEffect MOVEMENT_SPEED_EFFECT = new EnchantmentAttributeEffect(
            ResourceLocation.withDefaultNamespace("enchantment.soul_speed"),
            Attributes.MOVEMENT_SPEED,
            new PointerValue("soul_speed.movement_speed", new DiminishingReturnsValue(0.03F, 0.01F, 0.01F)),
            AttributeModifier.Operation.ADD_VALUE
    );

    public static final EnchantmentAttributeEffect MOVEMENT_EFFICIENCY_EFFECT = new EnchantmentAttributeEffect(
            ResourceLocation.withDefaultNamespace("enchantment.soul_speed"),
            Attributes.MOVEMENT_EFFICIENCY,
            new PointerValue("soul_speed.movement_efficiency", LevelBasedValue.constant(1.0F)),
            AttributeModifier.Operation.ADD_VALUE
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(SoulSpeed.DEFINITION.apply(context))
                    .withEffect(
                            EnchantmentEffectComponents.LOCATION_CHANGED,
                            SoulSpeed.MOVEMENT_SPEED_EFFECT,
                            AllOfCondition.allOf(
                                    InvertedLootItemCondition.invert(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().vehicle(EntityPredicate.Builder.entity()))),
                                    AnyOfCondition.anyOf(
                                            AllOfCondition.allOf(
                                                    EnchantmentActiveCheck.enchantmentActiveCheck(),
                                                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setIsFlying(false))),
                                                    AnyOfCondition.anyOf(
                                                            LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().movementAffectedBy(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.SOUL_SPEED_BLOCKS)))),
                                                            LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnGround(false)).build())
                                                    )
                                            ),
                                            AllOfCondition.allOf(
                                                    EnchantmentActiveCheck.enchantmentInactiveCheck(),
                                                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().movementAffectedBy(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.SOUL_SPEED_BLOCKS))).flags(EntityFlagsPredicate.Builder.flags().setIsFlying(false)))
                                            )
                                    )
                            )
                    )
                    .withEffect(
                            EnchantmentEffectComponents.LOCATION_CHANGED,
                            SoulSpeed.MOVEMENT_EFFICIENCY_EFFECT,
                            LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().movementAffectedBy(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.SOUL_SPEED_BLOCKS))))
                    )
                    .withEffect(
                            EnchantmentEffectComponents.LOCATION_CHANGED,
                            new DamageItem(new PointerValue("soul_speed.damage_item", LevelBasedValue.constant(1.0F))),
                            AllOfCondition.allOf(
                                    LootItemRandomChanceCondition.randomChance(EnchantmentLevelProvider.forEnchantmentLevel(new PointerValue("soul_speed.damage_item", LevelBasedValue.constant(0.04F)))),
                                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnGround(true)).movementAffectedBy(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.SOUL_SPEED_BLOCKS))))
                            )
                    )
                    .withEffect(
                            EnchantmentEffectComponents.TICK,
                            new SpawnParticlesEffect(ParticleTypes.SOUL, SpawnParticlesEffect.inBoundingBox(), SpawnParticlesEffect.offsetFromEntityPosition(0.1F), SpawnParticlesEffect.movementScaled(-0.2F), SpawnParticlesEffect.fixedVelocity(ConstantFloat.of(0.1F)), ConstantFloat.of(1.0F)),
                            LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, SoulSpeed.TICK_PREDICATE)
                    )
                    .withEffect(
                            EnchantmentEffectComponents.TICK,
                            new PlaySoundEffect(SoundEvents.SOUL_ESCAPE, ConstantFloat.of(0.6F), UniformFloat.of(0.6F, 1.0F)),
                            AllOfCondition.allOf(
                                    LootItemRandomChanceCondition.randomChance(0.35F),
                                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, SoulSpeed.TICK_PREDICATE)
                            )
                    )
                    .build(AdaptedEnchantments.SOUL_SPEED.location());
}