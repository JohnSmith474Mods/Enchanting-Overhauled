package johnsmith.api.enchantment.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import johnsmith.api.enchantment.theme.effect.EffectData;
import johnsmith.api.enchantment.theme.power.PowerProvider;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.List;
import java.util.Optional;

/**
 * Defines a "theme" for an enchantment.
 * This controls its visual style (color) and generation logic (power providers).
 *
 * @param name The display name of the theme.
 * @param colorCode Optional integer color code (e.g., 0xFFFFFF) for UI elements.
 * @param powerProviders A list of PowerProvider rules.
 * @param effects Optional particle and sound effects to spawn for this theme.
 */
public record EnchantmentTheme(
        Text name,
        Optional<Integer> colorCode,
        List<PowerProvider> powerProviders,
        Optional<EffectData> effects
) {
    public static final Codec<EnchantmentTheme> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TextCodecs.CODEC.fieldOf("name").forGetter(EnchantmentTheme::name),
                    Codec.INT.optionalFieldOf("color_code").forGetter(EnchantmentTheme::colorCode),
                    PowerProvider.CODEC.listOf().fieldOf("power_providers").forGetter(EnchantmentTheme::powerProviders),
                    EffectData.CODEC.optionalFieldOf("effects").forGetter(EnchantmentTheme::effects)
            ).apply(instance, EnchantmentTheme::new)
    );
}