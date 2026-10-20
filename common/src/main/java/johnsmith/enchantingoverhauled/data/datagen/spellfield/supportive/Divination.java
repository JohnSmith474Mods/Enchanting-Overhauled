package johnsmith.enchantingoverhauled.data.datagen.spellfield.supportive;

import johnsmith.enchantingoverhauled.api.enchantment.spellfield.*;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultMagicSchools;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultSpellFields;

import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.ProbabilityDistribution;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.audio.PlaySoundVolumeEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.entity.ApplyPotionEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.visual.AmbientFieldVisualEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.volume.LightningStrikeEffect;
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

public class Divination {

    public static final AiSpellField.AnchorPayload ANCHOR = new AiSpellField.AnchorPayload(
            64,
            1,
            0,
            2,
            10,
            Optional.empty()
    );

    public static final SpellFieldComponent COMPONENT = new SpellFieldComponent(
            List.of(DefaultSpellFields.linearSphereVolume(4.0F)),
            List.of(
                    new ApplyPotionEffect(
                            List.of(
                                    new PotionPayload(MobEffects.MOVEMENT_SPEED, LevelBasedValue.constant(100.0F), LevelBasedValue.constant(0.0F)),
                                    new PotionPayload(MobEffects.DAMAGE_BOOST, LevelBasedValue.constant(80.0F), LevelBasedValue.constant(0.0F))
                            ),
                            Optional.of(DefaultSpellFields.DO_NOT_HARM.get())
                    )
            ),
            List.of(),
            List.of(
                    new PlaySoundVolumeEffect(
                            DefaultSpellFields.soundHolder(SoundEvents.AMETHYST_BLOCK_CHIME),
                            SoundSource.HOSTILE,
                            LevelBasedValue.constant(1.0F),
                            LevelBasedValue.constant(1.3F)
                    ),
                    new LightningStrikeEffect(
                            LevelBasedValue.constant(1),
                            LevelBasedValue.constant(0),
                            LevelBasedValue.constant(0),
                            false,
                            Optional.of(new ProbabilityDistribution(15)),
                            Optional.of(DefaultSpellFields.CAN_HARM.get())
                    )
            ),
            List.of(new AmbientFieldVisualEffect(ParticleTypes.END_ROD, LevelBasedValue.constant(18.0F), LevelBasedValue.constant(0.03F), new ProbabilityDistribution(15)))
    );

    public static final Function<BootstrapContext<AiSpellField>, AiSpellField> SPELL_FIELD = (context) ->
            new AiSpellField(
                    10,
                    SpellRole.SUPPORTIVE,
                    SpellTarget.HOSTILE,
                    CastType.DIRECT,
                    DifficultyGate.EASY,
                    true,
                    Optional.of(DefaultSpellFields.schoolSet(context, DefaultMagicSchools.DIVINATION)),
                    EnchantmentThemes.COMMON,
                    Optional.of(Divination.ANCHOR),
                    Divination.COMPONENT
            );
}