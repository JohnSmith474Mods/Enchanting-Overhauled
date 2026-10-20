package johnsmith.enchantingoverhauled.data.datagen.spellfield.supportive;

import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultMagicSchools;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultSpellFields;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellField;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.CastType;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.DifficultyGate;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.SpellRole;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.SpellTarget;
import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;

import johnsmith.enchantmentcore.api.enchantment.spellfield.math.DistanceMetric;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldAxis;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.SpatialVector;
import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.ProbabilityDistribution;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.audio.PlaySoundVolumeEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.block.TemporaryTransmuteBlockEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.visual.VectorFieldVisualEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldTopology;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.TopologyEvaluator;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.ExponentialScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.LinearScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.CuboidShape;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.SpellFieldVolume;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Transmutation {

    public static final AiSpellField.AnchorPayload ANCHOR = new AiSpellField.AnchorPayload(
            3,
            1,
            2,
            0,
            0,
            Optional.empty()
    );

    public static final SpellFieldComponent COMPONENT = new SpellFieldComponent(
            List.of(new SpellFieldVolume(
                    Optional.of(new CuboidShape(
                            LevelBasedValue.constant(2.0F),
                            LevelBasedValue.constant(16.0F),
                            LevelBasedValue.constant(2.0F)
                    )),
                    Optional.of(new SpatialVector(
                            LevelBasedValue.constant(0.0F),
                            LevelBasedValue.constant(-16.0F),
                            LevelBasedValue.constant(0.0F),
                            LevelBasedValue.constant(0.0F),
                            LevelBasedValue.constant(0.0F),
                            LevelBasedValue.constant(0.0F)
                    )),
                    Optional.of(new FieldTopology(
                            LevelBasedValue.constant(32.0F),
                            Optional.of(LevelBasedValue.constant(32.0F)),
                            Optional.of(LevelBasedValue.constant(2.0F)),
                            Optional.of(new SpatialVector(
                                    LevelBasedValue.constant(0.0F),
                                    LevelBasedValue.constant(16.0F),
                                    LevelBasedValue.constant(0.0F),
                                    LevelBasedValue.constant(0.0F),
                                    LevelBasedValue.constant(0.0F),
                                    LevelBasedValue.constant(0.0F)
                            )),
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
                            DistanceMetric.EUCLIDEAN))
            )),
            List.of(),
            List.of(new TemporaryTransmuteBlockEffect(
                    Optional.empty(),
                    Blocks.AIR,
                    LevelBasedValue.constant(60.0F),
                    LevelBasedValue.constant(1.0F),
                    new TopologyEvaluator(Optional.of(LevelBasedValue.constant(0.1F)))
            )),
            List.of(new PlaySoundVolumeEffect(
                    DefaultSpellFields.soundHolder(SoundEvents.CHORUS_FRUIT_TELEPORT),
                    SoundSource.HOSTILE,
                    LevelBasedValue.constant(0.9F),
                    LevelBasedValue.constant(1.2F)
            )),
            List.of(new VectorFieldVisualEffect(
                    SimpleParticleTypes.SGA_RUNE.type(),
                    LevelBasedValue.constant(24.0F),
                    LevelBasedValue.constant(0.12F),
                    Optional.of(FieldAxis.VERTICAL),
                    new ProbabilityDistribution(15)
            ))
    );

    public static final Function<BootstrapContext<AiSpellField>, AiSpellField> SPELL_FIELD = (context) ->
            new AiSpellField(
                    10,
                    SpellRole.SUPPORTIVE,
                    SpellTarget.HOSTILE,
                    CastType.DIRECT,
                    DifficultyGate.HARD,
                    Optional.of(DefaultSpellFields.schoolSet(context, DefaultMagicSchools.TRANSMUTATION)),
                    EnchantmentThemes.COMMON,
                    Optional.of(Transmutation.ANCHOR),
                    Transmutation.COMPONENT
            );
}