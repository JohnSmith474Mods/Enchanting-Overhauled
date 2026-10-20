package johnsmith.enchantingoverhauled.item.enchantment.spellfield;

import com.mojang.serialization.MapCodec;

import johnsmith.enchantingoverhauled.Constants;

import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.SpellFieldEffect;
import johnsmith.enchantmentcore.api.registry.DataTransformerRegistry;
import johnsmith.enchantmentcore.api.registry.EnchantmentCoreRegistries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class NeoForgeSpellFieldEffects {
    public static void initialize(RegisterEvent event) {
        if (event.getRegistryKey().equals(EnchantmentCoreRegistries.SPELL_FIELD_EFFECT_KEY)) {
            Registry<MapCodec<? extends SpellFieldEffect>> registry = EnchantmentCoreRegistries.SPELL_FIELD_EFFECTS.get();
            for (SpellFieldEffects effect : SpellFieldEffects.values()) {
                if (effect.keyProvider() != null) {
                    for (String key : effect.keyProvider().getLevelBasedKeys()) {
                        DataTransformerRegistry.registerEffectProperty(key);
                    }
                }
                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, effect.getSerializedName());
                if (!registry.containsKey(location)) {
                    Registry.register(registry, location, effect.codec());
                }
            }
        }
    }

    private NeoForgeSpellFieldEffects() {}
}