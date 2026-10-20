package johnsmith.enchantingoverhauled.data.datagen.spellfield.supportive;

import johnsmith.enchantingoverhauled.api.enchantment.spellfield.*;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultMagicSchools;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultSpellFields;

import johnsmith.enchantmentcore.api.enchantment.spellfield.math.DistanceMetric;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldAxis;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.SpatialVector;
import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.ProbabilityDistribution;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.audio.PlaySoundVolumeEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.entity.HealEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.visual.BurstEntityVisualEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.visual.VectorFieldVisualEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldTopology;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.ConstantScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.LinearScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.CubeShape;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.SpellFieldVolume;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Perpetuation {

    public static final SpellFieldComponent COMPONENT = new SpellFieldComponent(
            List.of(new SpellFieldVolume(
                    Optional.of(new CubeShape(LevelBasedValue.constant(2F))),
                    Optional.of(new SpatialVector(
                            LevelBasedValue.constant(0.F),
                            LevelBasedValue.constant(2F),
                            LevelBasedValue.constant(0.F),
                            LevelBasedValue.constant(0.F),
                            LevelBasedValue.constant(0.F),
                            LevelBasedValue.constant(0.F)
                    )),
                    Optional.of(new FieldTopology(
                            LevelBasedValue.constant(2F),
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
            List.of(
                    new HealEffect(LevelBasedValue.constant(12.0F), Optional.of(DefaultSpellFields.DO_NOT_HARM.get())),
                    new BurstEntityVisualEffect(ParticleTypes.HAPPY_VILLAGER, LevelBasedValue.constant(8.0F), LevelBasedValue.constant(0.1F))
            ),
            List.of(),
            List.of(new PlaySoundVolumeEffect(DefaultSpellFields.soundHolder(SoundEvents.BEACON_AMBIENT), SoundSource.HOSTILE, LevelBasedValue.constant(0.9F), LevelBasedValue.constant(1.15F))),
            List.of(new VectorFieldVisualEffect(ParticleTypes.HAPPY_VILLAGER, LevelBasedValue.constant(20.0F), LevelBasedValue.constant(0.08F), Optional.of(FieldAxis.VERTICAL), new ProbabilityDistribution(15)))
    );

    public static final Function<BootstrapContext<AiSpellField>, AiSpellField> SPELL_FIELD = (context) ->
            new AiSpellField(
                    10,
                    SpellRole.SUPPORTIVE,
                    SpellTarget.ALLY,
                    CastType.DIRECT,
                    DifficultyGate.EASY,
                    Optional.of(DefaultSpellFields.schoolSet(context, DefaultMagicSchools.PERPETUATION)),
                    EnchantmentThemes.COMMON,
                    Optional.empty(),
                    Perpetuation.COMPONENT
            );
}