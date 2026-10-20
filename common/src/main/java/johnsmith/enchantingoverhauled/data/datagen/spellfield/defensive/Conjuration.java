package johnsmith.enchantingoverhauled.data.datagen.spellfield.defensive;

import johnsmith.enchantingoverhauled.api.enchantment.spellfield.*;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultMagicSchools;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultSpellFields;

import johnsmith.enchantmentcore.api.enchantment.spellfield.math.DistanceMetric;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.SpatialVector;
import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.ProbabilityDistribution;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.audio.PlaySoundVolumeEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.block.PlaceTemporaryBlockEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.visual.AmbientFieldVisualEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldTopology;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.TopologyEvaluator;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.ExponentialScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.LinearScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.CuboidShape;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.SpellFieldVolume;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Conjuration {

    public static final AiSpellField.AnchorPayload ANCHOR = new AiSpellField.AnchorPayload(
            5,
            1,
            4,
            0,
            0,
            Optional.empty()
    );

    public static final SpellFieldComponent COMPONENT = new SpellFieldComponent(
            List.of(new SpellFieldVolume(
                    Optional.of(new CuboidShape(
                            LevelBasedValue.constant(1.5F),
                            LevelBasedValue.constant(4.0F),
                            LevelBasedValue.constant(1.5F)
                    )),
                    Optional.of(new SpatialVector(
                            LevelBasedValue.constant(0.0F),
                            LevelBasedValue.constant(1.0F),
                            LevelBasedValue.constant(0.0F),
                            LevelBasedValue.constant(0.0F),
                            LevelBasedValue.constant(0.0F),
                            LevelBasedValue.constant(0.0F)
                    )),
                    Optional.of(new FieldTopology(
                            LevelBasedValue.constant(6.0F),
                            Optional.of(LevelBasedValue.constant(6.0F)),
                            Optional.of(LevelBasedValue.constant(1.5F)),
                            Optional.empty(),
                            Optional.of(new SpatialVector(
                                    LevelBasedValue.constant(0.0F),
                                    LevelBasedValue.constant(1.0F),
                                    LevelBasedValue.constant(0.0F),
                                    LevelBasedValue.constant(0.0F),
                                    LevelBasedValue.constant(0.0F),
                                    LevelBasedValue.constant(0.0F)
                            )),
                            new ExponentialScalingFunction(0.1F),
                            new LinearScalingFunction(),
                            new LinearScalingFunction(),
                            DistanceMetric.EUCLIDEAN
                    ))
            )),
            List.of(),
            List.of(
                    new PlaceTemporaryBlockEffect(
                            Blocks.POWDER_SNOW,
                            LevelBasedValue.constant(160.0F),
                            LevelBasedValue.constant(20.0F),
                            false,
                            true,
                            new TopologyEvaluator(Optional.of(LevelBasedValue.constant(0.6F)))
                    ),
                    new PlaceTemporaryBlockEffect(
                            Blocks.PACKED_ICE,
                            LevelBasedValue.constant(140.0F),
                            LevelBasedValue.constant(20.0F),
                            false,
                            true,
                            new TopologyEvaluator(Optional.of(LevelBasedValue.constant(0.2F)))
                    ),
                    new PlaceTemporaryBlockEffect(
                            Blocks.ICE,
                            LevelBasedValue.constant(120.0F),
                            LevelBasedValue.constant(20.0F),
                            false,
                            true,
                            new TopologyEvaluator(Optional.of(LevelBasedValue.constant(0.1F)))
                    )
            ),
            List.of(new PlaySoundVolumeEffect(DefaultSpellFields.soundHolder(SoundEvents.GLASS_BREAK), SoundSource.HOSTILE, LevelBasedValue.constant(0.85F), LevelBasedValue.constant(1.2F))),
            List.of(new AmbientFieldVisualEffect(ParticleTypes.SNOWFLAKE, LevelBasedValue.constant(22.0F), LevelBasedValue.constant(0.05F), new ProbabilityDistribution(15)))
    );

    public static final Function<BootstrapContext<AiSpellField>, AiSpellField> SPELL_FIELD = (context) ->
            new AiSpellField(
                    10,
                    SpellRole.DEFENSIVE,
                    SpellTarget.HOSTILE,
                    CastType.DIRECT,
                    DifficultyGate.HARD,
                    Optional.of(DefaultSpellFields.schoolSet(context, DefaultMagicSchools.CONJURATION)),
                    EnchantmentThemes.COMMON,
                    Optional.of(Conjuration.ANCHOR),
                    Conjuration.COMPONENT
            );
}