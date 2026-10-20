package johnsmith.enchantingoverhauled.data.datagen;

import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellField;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultSpellFields;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.defensive.Abjuration;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.defensive.Conjuration;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.defensive.Deception;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.offensive.Coercion;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.offensive.Obliteration;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.offensive.Translation;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.supportive.Divination;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.supportive.Perpetuation;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.supportive.Transmutation;

import net.minecraft.data.worldgen.BootstrapContext;

public final class AiSpellFieldRegistryBootstrap {
    public static void bootstrap(BootstrapContext<AiSpellField> context) {
        context.register(DefaultSpellFields.OBLITERATION, Obliteration.SPELL_FIELD.apply(context));
        context.register(DefaultSpellFields.ABJURATION, Abjuration.SPELL_FIELD.apply(context));
        context.register(DefaultSpellFields.PERPETUATION, Perpetuation.SPELL_FIELD.apply(context));
        context.register(DefaultSpellFields.COERCION, Coercion.SPELL_FIELD.apply(context));
        context.register(DefaultSpellFields.DECEPTION, Deception.SPELL_FIELD.apply(context));
        context.register(DefaultSpellFields.DIVINATION, Divination.SPELL_FIELD.apply(context));
        context.register(DefaultSpellFields.TRANSLATION, Translation.SPELL_FIELD.apply(context));
        context.register(DefaultSpellFields.CONJURATION, Conjuration.SPELL_FIELD.apply(context));
        context.register(DefaultSpellFields.TRANSMUTATION, Transmutation.SPELL_FIELD.apply(context));
    }

    private AiSpellFieldRegistryBootstrap() {}
}