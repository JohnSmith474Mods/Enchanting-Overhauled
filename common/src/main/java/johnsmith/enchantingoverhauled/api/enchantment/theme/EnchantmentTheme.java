package johnsmith.enchantingoverhauled.api.enchantment.theme;

import johnsmith.enchantingoverhauled.api.enchantment.theme.effect.EffectData;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.PowerProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.List;
import java.util.Optional;

/**
 * A data-driven record defining a complete "theme" for a set of related enchantments.
 * <p>
 * A theme controls the enchantment's metadata (display name and color), its generation logic
 * (which nearby blocks provide thematic enchanting power), and its visual/auditory feedback
 * at the Enchanting Altar and on flying book entities.
 *
 * @param name           The display name {@link Component} of the theme, used for localization and UI.
 * @param colorCode      An optional integer RGB color code (e.g., {@code 0xFFFFFF}) used for coloring UI elements,
 *                       item prefixes, and enchantment names in tooltips.
 * @param powerProviders A list of {@link PowerProvider} rules that specify which blocks contribute
 *                       enchanting power for this theme and how much.
 * @param effects        An optional {@link EffectData} wrapper containing the particle and sound effects to spawn
 *                       when this theme is dominant at the enchanting table or on a flying book.
 * @see EnchantmentThemes
 */
public record EnchantmentTheme(
        Component name,
        Optional<Integer> colorCode,
        List<PowerProvider> powerProviders,
        Optional<EffectData> effects
) {

    /**
     * The codec responsible for serializing and deserializing full instances of this record from data files (JSON).
     */
    public static final Codec<EnchantmentTheme> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance ->
            instance.group(
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(EnchantmentTheme::name),
                    Codec.INT.optionalFieldOf("color_code").forGetter(EnchantmentTheme::colorCode),
                    PowerProvider.CODEC.listOf().fieldOf("power_providers").forGetter(EnchantmentTheme::powerProviders),
                    EffectData.CODEC.optionalFieldOf("effects").forGetter(EnchantmentTheme::effects)
            ).apply(instance, EnchantmentTheme::new)
    ));

    /**
     * A lightweight network codec that omits {@link #powerProviders} when synchronizing visual theme
     * metadata to clients.
     */
    public static final Codec<EnchantmentTheme> NETWORK_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance ->
            instance.group(
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(EnchantmentTheme::name),
                    Codec.INT.optionalFieldOf("color_code").forGetter(EnchantmentTheme::colorCode),
                    EffectData.CODEC.optionalFieldOf("effects").forGetter(EnchantmentTheme::effects)
            ).apply(instance, (name, colorCode, effects) -> new EnchantmentTheme(name, colorCode, List.of(), effects))
    ));
}