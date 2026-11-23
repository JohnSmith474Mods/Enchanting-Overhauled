package johnsmith.enchantingoverhauled.data.datagen.spellfield.defensive;

import johnsmith.enchantingoverhauled.api.enchantment.spellfield.*;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.data.datagen.RegistryLookup;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultMagicSchools;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultSpellFields;
import johnsmith.enchantingoverhauled.entity.Entities;
import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.DistanceMetric;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldAxis;
import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.ProbabilityDistribution;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.entity.ImpulseEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.visual.VectorFieldVisualEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldTopology;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.ConstantScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.CubeShape;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.SpellFieldVolume;

import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Abjuration {

    public static final Function<BootstrapContext<AiSpellField>, Holder<LootItemCondition>> DEFLECTABLE_PROJECTILE_FILTER = context -> Holder.direct(
            AllOfCondition.allOf(
                    LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity().of(RegistryLookup.ENTITY_TYPE.apply(context), EntityTypeTags.IMPACT_PROJECTILES)
                    ),
                    InvertedLootItemCondition.invert(
                            LootItemEntityPropertyCondition.hasProperties(
                                    LootContext.EntityTarget.THIS,
                                    EntityPredicate.Builder.entity().of(RegistryLookup.ENTITY_TYPE.apply(context), Entities.MAGIC_MISSILE.get())
                            )
                    )
            ).build()
    );

    public static final AiSpellField.AnchorPayload ANCHOR = new AiSpellField.AnchorPayload(
            600,
            1,
            0,
            0,
            0,
            Optional.empty()
    );

    public static final Function<BootstrapContext<AiSpellField>, SpellFieldComponent> COMPONENT = context -> new SpellFieldComponent(
            List.of(new SpellFieldVolume(
                    Optional.of(new CubeShape(LevelBasedValue.constant(6.0F))),
                    Optional.empty(),
                    Optional.of(new FieldTopology(
                            LevelBasedValue.constant(6.0F),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            new ConstantScalingFunction(1.0F),
                            ConstantScalingFunction.NONE,
                            ConstantScalingFunction.NONE,
                            DistanceMetric.EUCLIDEAN
                    )))),
            List.of(
                    new ImpulseEffect(
                            LevelBasedValue.constant(2.0F),
                            Optional.of(FieldAxis.RADIAL),
                            Optional.of(DEFLECTABLE_PROJECTILE_FILTER.apply(context))
                    )
            ),
            List.of(),
            List.of(),
            List.of(
                    new VectorFieldVisualEffect(
                            SimpleParticleTypes.SGA_RUNE.type(),
                            LevelBasedValue.constant(4.0F),
                            LevelBasedValue.constant(0.16F),
                            Optional.of(FieldAxis.RADIAL),
                            new ProbabilityDistribution(15)
                    )
            )
    );

    public static final Function<BootstrapContext<AiSpellField>, AiSpellField> SPELL_FIELD = (context) ->
            new AiSpellField(
                    10,
                    SpellRole.DEFENSIVE,
                    SpellTarget.SELF,
                    CastType.DIRECT,
                    DifficultyGate.NORMAL,
                    Optional.of(DefaultSpellFields.schoolSet(context, DefaultMagicSchools.ABJURATION)),
                    EnchantmentThemes.COMMON,
                    Optional.of(Abjuration.ANCHOR),
                    Abjuration.COMPONENT.apply(context)
            );
}