package johnsmith.enchantingoverhauled.data.datagen.spellfield.offensive;

import johnsmith.enchantingoverhauled.api.enchantment.spellfield.*;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultMagicSchools;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultSpellFields;
import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;

import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldAxis;
import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.ProbabilityDistribution;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.audio.PlaySoundVolumeEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.entity.AbsoluteStasisEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.entity.ApplyPotionEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.visual.BurstEntityVisualEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.visual.VectorFieldVisualEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.type.PotionPayload;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Coercion {

    public static final AiSpellField.AnchorPayload ANCHOR = new AiSpellField.AnchorPayload(
            60,
            1,
            0,
            0,
            0,
            Optional.empty()
    );

    public static final SpellFieldComponent COMPONENT = new SpellFieldComponent(
            List.of(DefaultSpellFields.linearSphereVolume(4.0F)),
            List.of(
                    new ApplyPotionEffect(List.of(
                            new PotionPayload(MobEffects.MOVEMENT_SLOWDOWN, LevelBasedValue.constant(2), LevelBasedValue.constant(255)),
                            new PotionPayload(MobEffects.DIG_SLOWDOWN, LevelBasedValue.constant(2), LevelBasedValue.constant(255)),
                            new PotionPayload(MobEffects.WEAKNESS, LevelBasedValue.constant(2), LevelBasedValue.constant(255))
                    ), Optional.of(DefaultSpellFields.CAN_HARM.get())),
                    new AbsoluteStasisEffect(Optional.of(DefaultSpellFields.CAN_HARM.get())),
                    new BurstEntityVisualEffect(ParticleTypes.ENCHANTED_HIT, LevelBasedValue.constant(10.0F), LevelBasedValue.constant(0.15F))
            ),
            List.of(),
            List.of(new PlaySoundVolumeEffect(DefaultSpellFields.soundHolder(SoundEvents.PLAYER_ATTACK_CRIT), SoundSource.HOSTILE, LevelBasedValue.constant(0.9F), LevelBasedValue.constant(0.9F))),
            List.of(new VectorFieldVisualEffect(SimpleParticleTypes.SGA_RUNE.type(), LevelBasedValue.constant(22.0F), LevelBasedValue.constant(-0.15F), Optional.of(FieldAxis.RADIAL), new ProbabilityDistribution(15)))
    );

    public static final Function<BootstrapContext<AiSpellField>, AiSpellField> SPELL_FIELD = (context) ->
            new AiSpellField(
                    10,
                    SpellRole.OFFENSIVE,
                    SpellTarget.HOSTILE,
                    CastType.PROJECTILE,
                    DifficultyGate.EASY,
                    Optional.of(DefaultSpellFields.schoolSet(context, DefaultMagicSchools.COERCION)),
                    EnchantmentThemes.COMMON,
                    Optional.of(Coercion.ANCHOR),
                    Coercion.COMPONENT
            );
}