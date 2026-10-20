package johnsmith.enchantingoverhauled.data.datagen.spellfield.offensive;

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
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.entity.DamageEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.entity.ImpulseEffect;
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
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Translation {

    public static final AiSpellField.AnchorPayload ANCHOR = new AiSpellField.AnchorPayload(
            68,
            2,
            4,
            0,
            0,
            Optional.empty()
    );

    public static final SpellFieldComponent COMPONENT = new SpellFieldComponent(
            List.of(new SpellFieldVolume(
                    Optional.of(new CubeShape(LevelBasedValue.constant(3.F))),
                    Optional.of(new SpatialVector(
                            LevelBasedValue.constant(0.F),
                            LevelBasedValue.constant(-0.25F),
                            LevelBasedValue.constant(0.F),
                            LevelBasedValue.constant(0.F),
                            LevelBasedValue.constant(0.F),
                            LevelBasedValue.constant(0.F)
                    )),
                    Optional.of(new FieldTopology(
                            LevelBasedValue.constant(3.F),
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
            List.of(new ImpulseEffect(LevelBasedValue.constant(8.0F),
                            Optional.of(FieldAxis.RADIAL),
                            Optional.of(DefaultSpellFields.CAN_HARM.get())),
                    new DamageEffect(DamageTypes.WIND_CHARGE, LevelBasedValue.constant(2.F), Optional.of(DefaultSpellFields.CAN_HARM.get())),
                    new BurstEntityVisualEffect(ParticleTypes.SMALL_GUST, LevelBasedValue.constant(16.0F), LevelBasedValue.constant(0.2F))
            ),
            List.of(),
            List.of(new PlaySoundVolumeEffect(DefaultSpellFields.soundHolder(SoundEvents.WIND_CHARGE_BURST.value()), SoundSource.HOSTILE, LevelBasedValue.constant(0.9F), LevelBasedValue.constant(1.15F))),
            List.of(new VectorFieldVisualEffect(ParticleTypes.SMALL_GUST, LevelBasedValue.constant(26.0F), LevelBasedValue.constant(0.2F), Optional.of(FieldAxis.RADIAL), new ProbabilityDistribution(15)))
    );

    public static final Function<BootstrapContext<AiSpellField>, AiSpellField> SPELL_FIELD = (context) ->
            new AiSpellField(
                    10,
                    SpellRole.OFFENSIVE,
                    SpellTarget.HOSTILE,
                    CastType.PROJECTILE,
                    DifficultyGate.HARD,
                    Optional.of(DefaultSpellFields.schoolSet(context, DefaultMagicSchools.TRANSLATION)),
                    EnchantmentThemes.COMMON,
                    Optional.of(Translation.ANCHOR),
                    Translation.COMPONENT
            );
}