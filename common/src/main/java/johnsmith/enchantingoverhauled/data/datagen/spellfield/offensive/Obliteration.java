package johnsmith.enchantingoverhauled.data.datagen.spellfield.offensive;

import johnsmith.enchantingoverhauled.api.enchantment.spellfield.*;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.api.enchantment.value.DifficultyBasedValue;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultMagicSchools;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultSpellFields;
import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;

import johnsmith.enchantmentcore.api.enchantment.spellfield.math.DistanceMetric;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldAxis;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.SpatialVector;
import johnsmith.enchantmentcore.api.enchantment.effect.ExplosionDefinition;
import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.ProbabilityDistribution;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.visual.VectorFieldVisualEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.volume.ExplodeEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldTopology;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.ConstantScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.LinearScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.CubeShape;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.SpellFieldVolume;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Obliteration {

    public static final AiSpellField.AnchorPayload ANCHOR = new AiSpellField.AnchorPayload(
            64,
            4,
            0,
            0,
            0,
            Optional.empty()
    );

    public static final SpellFieldComponent COMPONENT = new SpellFieldComponent(
            List.of(new SpellFieldVolume(
                    Optional.of(new CubeShape(new DifficultyBasedValue(LevelBasedValue.perLevel(1.0F, 1.0F)))),
                    Optional.of(new SpatialVector(
                            LevelBasedValue.constant(0.F),
                            new DifficultyBasedValue(LevelBasedValue.perLevel(1.0F, 1.0F)),
                            LevelBasedValue.constant(0.F),
                            LevelBasedValue.constant(0.F),
                            LevelBasedValue.constant(0.F),
                            LevelBasedValue.constant(0.F)
                    )),
                    Optional.of(new FieldTopology(
                            new DifficultyBasedValue(LevelBasedValue.perLevel(1.0F, 1.0F)),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            new LinearScalingFunction(),
                            ConstantScalingFunction.NONE,
                            ConstantScalingFunction.NONE,
                            DistanceMetric.EUCLIDEAN
                    ))
            )),
            List.of(),
            List.of(),
            List.of(
                    new ExplodeEffect(
                            new ExplosionDefinition(
                                    new DifficultyBasedValue(LevelBasedValue.perLevel(1.0F, 2.0F)),
                                    false,
                                    true,
                                    ExplosionDefinition.ExplosionInteraction.NONE,
                                    Optional.of(DefaultSpellFields.DO_NOT_HARM.get()),
                                    Optional.empty(),
                                    Optional.empty(),
                                    Optional.empty()
                            ),
                            LevelBasedValue.constant(1.0F),
                            LevelBasedValue.constant(0.0F),
                            LevelBasedValue.constant(0.0F),
                            LevelBasedValue.constant(0.0F),
                            Optional.of(new ProbabilityDistribution(15)),
                            Optional.empty()
                    )
            ),
            List.of(
                    new VectorFieldVisualEffect(
                            SimpleParticleTypes.SGA_RUNE.type(),
                            LevelBasedValue.constant(24.0F),
                            LevelBasedValue.constant(0.18F),
                            Optional.of(FieldAxis.RADIAL),
                            new ProbabilityDistribution(15)
                    )
            )
    );

    public static final Function<BootstrapContext<AiSpellField>, AiSpellField> SPELL_FIELD = (context) ->
            new AiSpellField(
                    10,
                    SpellRole.OFFENSIVE,
                    SpellTarget.HOSTILE,
                    CastType.PROJECTILE,
                    DifficultyGate.NORMAL,
                    Optional.of(DefaultSpellFields.schoolSet(context, DefaultMagicSchools.OBLITERATION)),
                    EnchantmentThemes.COMMON,
                    Optional.of(Obliteration.ANCHOR),
                    Obliteration.COMPONENT
            );
}