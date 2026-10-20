package johnsmith.enchantingoverhauled.data.datagen.spellfield.defensive;

import johnsmith.enchantingoverhauled.api.enchantment.spellfield.effect.SummonMirageEffect;
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
import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.ProbabilityDistribution;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.audio.PlaySoundVolumeEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.visual.AmbientFieldVisualEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldTopology;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.ConstantScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.SpellFieldVolume;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Deception {

    public static final SpellFieldComponent COMPONENT = new SpellFieldComponent(
            List.of(new SpellFieldVolume(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(new FieldTopology(
                            LevelBasedValue.constant(8.0F),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            new ConstantScalingFunction(1.0F),
                            ConstantScalingFunction.NONE,
                            ConstantScalingFunction.NONE,
                            DistanceMetric.EUCLIDEAN
                    ))
            )),
            List.of(),
            List.of(),
            List.of(
                    new SummonMirageEffect(
                            LevelBasedValue.constant(3.F),
                            LevelBasedValue.constant(600.F)
                    ),
                    new PlaySoundVolumeEffect(
                            DefaultSpellFields.soundHolder(SoundEvents.ILLUSIONER_MIRROR_MOVE),
                            SoundSource.HOSTILE,
                            LevelBasedValue.constant(0.95F),
                            LevelBasedValue.constant(1.05F)
                    )
            ),
            List.of(
                    new AmbientFieldVisualEffect(
                            SimpleParticleTypes.SGA_RUNE.type(),
                            LevelBasedValue.constant(24.0F),
                            LevelBasedValue.constant(0.05F),
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
                    DifficultyGate.EASY,
                    Optional.of(DefaultSpellFields.schoolSet(context, DefaultMagicSchools.DECEPTION)),
                    EnchantmentThemes.COMMON,
                    Optional.empty(),
                    Deception.COMPONENT
            );
}