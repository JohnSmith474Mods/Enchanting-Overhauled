package johnsmith.enchantingoverhauled.data.datagen;

import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultMagicSchools;

import net.minecraft.data.worldgen.BootstrapContext;

import java.util.Optional;

public final class MagicSchoolRegistryBootstrap {
    public static void bootstrap(BootstrapContext<MagicSchool> context) {
        context.register(DefaultMagicSchools.ABJURATION, new MagicSchool("abjuration", "magic_school.enchanting_overhauled.abjuration", Optional.of(5592575)));
        context.register(DefaultMagicSchools.OBLITERATION, new MagicSchool("obliteration", "magic_school.enchanting_overhauled.obliteration", Optional.of(16733525)));
        context.register(DefaultMagicSchools.PERPETUATION, new MagicSchool("perpetuation", "magic_school.enchanting_overhauled.perpetuation", Optional.of(5635925)));
        context.register(DefaultMagicSchools.DECEPTION, new MagicSchool("deception", "magic_school.enchanting_overhauled.deception", Optional.of(5592575)));
        context.register(DefaultMagicSchools.COERCION, new MagicSchool("coercion", "magic_school.enchanting_overhauled.coercion", Optional.of(16733525)));
        context.register(DefaultMagicSchools.DIVINATION, new MagicSchool("divination", "magic_school.enchanting_overhauled.divination", Optional.of(5635925)));
        context.register(DefaultMagicSchools.CONJURATION, new MagicSchool("conjuration", "magic_school.enchanting_overhauled.conjuration", Optional.of(5592575)));
        context.register(DefaultMagicSchools.TRANSLATION, new MagicSchool("translation", "magic_school.enchanting_overhauled.translation", Optional.of(16733525)));
        context.register(DefaultMagicSchools.TRANSMUTATION, new MagicSchool("transmutation", "magic_school.enchanting_overhauled.transmutation", Optional.of(5635925)));
    }
}
