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
 * A theme controls the enchantment's metadata (color, name), its generation logic (which blocks provide power),
 * and its visual/auditory feedback at the enchanting table.
 *
 * @param name             The display name of the theme, used for localization and UI.
 * @param colorCode        An optional integer color code (e.g., {@code 0xFFFFFF}) used for coloring UI elements,
 * such as the enchantment name in the tooltips.
 * @param powerProviders   A list of {@link PowerProvider} rules that specify which blocks contribute
 * enchanting power for this theme, and how much.
 * @param effects          An optional wrapper containing the particle and sound effects to spawn when
 * this theme is dominant at the enchanting table.
 */
public record EnchantmentTheme(
        Component name,
        Optional<Integer> colorCode,
        List<PowerProvider> powerProviders,
        Optional<EffectData> effects
) {
    /**
     * The codec responsible for serializing and deserializing instances of this record from data files (e.g., JSON).
     */
    public static final Codec<EnchantmentTheme> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(EnchantmentTheme::name),
                    Codec.INT.optionalFieldOf("color_code").forGetter(EnchantmentTheme::colorCode),
                    PowerProvider.CODEC.listOf().fieldOf("power_providers").forGetter(EnchantmentTheme::powerProviders),
                    EffectData.CODEC.optionalFieldOf("effects").forGetter(EnchantmentTheme::effects)
            ).apply(instance, EnchantmentTheme::new)
    );
}