package johnsmith.enchantingoverhauled.api.enchantment.school;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * A data-driven record representing a School of Magic in Enchanting Overhauled.
 * <p>
 * Schools of Magic categorize enchantments by their functional discipline (e.g., Abjuration,
 * Obliteration, Perpetuation, Deception, Coercion, Divination, Conjuration, Translation, Transmutation)
 * and govern item naming suffixes, particle/missile tinting, and AI spell selection affinities.
 *
 * @param id             The internal string identifier of the magic school.
 * @param translationKey The localization key used to display the school's name in tooltips and item titles.
 * @param colorCode      An optional integer RGB/ARGB color code used to tint particles, projectiles, and text.
 * @see MagicSchools
 */
public record MagicSchool(
        String id,
        String translationKey,
        Optional<Integer> colorCode
) {

    /**
     * The default fallback color ({@code 0xFFFFFFFF}, opaque white) used when {@link #colorCode} is empty.
     */
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;

    /**
     * The codec responsible for serializing and deserializing {@link MagicSchool} definitions from JSON.
     */
    public static final Codec<MagicSchool> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(MagicSchool::id),
                    Codec.STRING.fieldOf("translation_key").forGetter(MagicSchool::translationKey),
                    Codec.INT.optionalFieldOf("color_code").forGetter(MagicSchool::colorCode)
            ).apply(instance, MagicSchool::new)
    ));

    /**
     * Resolves the integer color code of this magic school, falling back to {@link #DEFAULT_COLOR}
     * if no explicit color code is defined.
     *
     * @return The integer color value.
     */
    public int getColor() {
        return this.colorCode.orElse(DEFAULT_COLOR);
    }
}