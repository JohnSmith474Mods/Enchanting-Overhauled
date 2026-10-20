package johnsmith.enchantingoverhauled.item.enchantment.spellfield;

import com.mojang.serialization.MapCodec;

import johnsmith.enchantingoverhauled.api.enchantment.spellfield.effect.SummonMirageEffect;

import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.SpellFieldEffect;
import johnsmith.enchantmentcore.api.registry.LevelBasedKeyProvider;
import johnsmith.enchantmentcore.api.registry.DataTransformerRegistry;
import johnsmith.enchantmentcore.api.registry.EnchantmentCoreRegistries;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Central registrar for {@code enchanting_overhauled} custom {@link SpellFieldEffect} codecs.
 * <p>
 * Registers each effect's {@link LevelBasedKeyProvider} keys with {@link DataTransformerRegistry}
 * and binds the effect codecs into {@link EnchantmentCoreRegistries#SPELL_FIELD_EFFECTS}.
 */
public enum SpellFieldEffects implements StringRepresentable {
    SUMMON_MIRAGE(
            SummonMirageEffect.KEY,
            SummonMirageEffect.KEY_PROVIDER,
            SummonMirageEffect.CODEC
    );

    private final String key;
    private final LevelBasedKeyProvider keyProvider;
    private final MapCodec<? extends SpellFieldEffect> codec;

    SpellFieldEffects(String key, LevelBasedKeyProvider keyProvider, MapCodec<? extends SpellFieldEffect> codec) {
        this.key = key;
        this.keyProvider = keyProvider;
        this.codec = codec;
    }

    public LevelBasedKeyProvider keyProvider() {
        return this.keyProvider;
    }

    public MapCodec<? extends  SpellFieldEffect> codec() {
        return this.codec;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.key;
    }
}