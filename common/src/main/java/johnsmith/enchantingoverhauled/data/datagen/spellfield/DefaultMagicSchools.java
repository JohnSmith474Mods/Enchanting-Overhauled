package johnsmith.enchantingoverhauled.data.datagen.spellfield;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class DefaultMagicSchools {
    public static final ResourceKey<MagicSchool> ABJURATION = schoolKey("abjuration");
    public static final ResourceKey<MagicSchool> OBLITERATION = schoolKey("obliteration");
    public static final ResourceKey<MagicSchool> PERPETUATION = schoolKey("perpetuation");
    public static final ResourceKey<MagicSchool> DECEPTION = schoolKey("deception");
    public static final ResourceKey<MagicSchool> COERCION = schoolKey("coercion");
    public static final ResourceKey<MagicSchool> DIVINATION = schoolKey("divination");
    public static final ResourceKey<MagicSchool> CONJURATION = schoolKey("conjuration");
    public static final ResourceKey<MagicSchool> TRANSLATION = schoolKey("translation");
    public static final ResourceKey<MagicSchool> TRANSMUTATION = schoolKey("transmutation");

    private static ResourceKey<MagicSchool> schoolKey(String name) {
        return ResourceKey.create(MagicSchools.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
    }
}
