package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.advancements.critereon.MovementPredicate;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.ExplodeEffect;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Function;

public class WindBurst {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.MACE_ENCHANTABLE),
            2,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(15, 9),
            Enchantment.dynamicCost(65, 9),
            4,
            EquipmentSlotGroup.MAINHAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(WindBurst.DEFINITION.apply(context))
                    .withEffect(
                            EnchantmentEffectComponents.POST_ATTACK,
                            EnchantmentTarget.ATTACKER,
                            EnchantmentTarget.ATTACKER,
                            new ExplodeEffect(
                                    false,
                                    Optional.empty(),
                                    Optional.of(new PointerValue("wind_burst.post_attack.explode_effect", new DiminishingReturnsValue(1.2F, 0.65F, 0.45F))),
                                    context.lookup(Registries.BLOCK).get(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity()),
                                    Vec3.ZERO,
                                    LevelBasedValue.constant(3.5F),
                                    false,
                                    ExplosionInteraction.TRIGGER,
                                    ParticleTypes.GUST_EMITTER_SMALL,
                                    ParticleTypes.GUST_EMITTER_LARGE,
                                    SoundEvents.WIND_CHARGE_BURST
                            ),
                            LootItemEntityPropertyCondition.hasProperties(
                                    LootContext.EntityTarget.DIRECT_ATTACKER,
                                    EntityPredicate.Builder.entity()
                                            .flags(EntityFlagsPredicate.Builder.flags().setIsFlying(false))
                                            .moving(MovementPredicate.fallDistance(Doubles.atLeast(1.5F)))
                            )
                    )
                    .build(AdaptedEnchantments.WIND_BURST.location());
}