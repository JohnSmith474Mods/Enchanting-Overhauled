package johnsmith.enchantingoverhauled.item.enchantment.spellfield;

import com.mojang.serialization.MapCodec;

import johnsmith.enchantingoverhauled.Constants;

import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.SpellFieldEffect;
import johnsmith.enchantmentcore.api.registry.EnchantmentCoreRegistries;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Handles Fabric-side registration of custom {@link SpellFieldEffect} codecs for Enchanting Overhauled.
 * <p>
 * Because Fabric Loader does not topologically sort {@code main} entrypoints by {@code fabric.mod.json}
 * dependencies, {@code enchanting_overhauled} can execute {@code onInitialize()} before
 * {@code enchantment_core} has created the {@link EnchantmentCoreRegistries#SPELL_FIELD_EFFECT_KEY} registry.
 * This class checks whether {@link EnchantmentCoreRegistries#SPELL_FIELD_EFFECTS} is already present and,
 * if not, defers registration via {@link RegistryEntryAddedCallback} until {@code enchantment_core}
 * registers the registry into {@link BuiltInRegistries#REGISTRY}.
 */
public final class FabricSpellFieldEffects {

    private static boolean registered = false;

    private FabricSpellFieldEffects() {}

    @SuppressWarnings("unchecked")
    public static void initialize() {
        Registry<MapCodec<? extends SpellFieldEffect>> existing = EnchantmentCoreRegistries.SPELL_FIELD_EFFECTS.get();
        if (existing != null) {
            registerAll(existing);
            return;
        }

        ResourceLocation registryId = EnchantmentCoreRegistries.SPELL_FIELD_EFFECT_KEY.location();

        // Enchantment Core's main entrypoint hasn't run yet; register as soon as it adds the registry
        RegistryEntryAddedCallback.event(BuiltInRegistries.REGISTRY).register((rawId, id, object) -> {
            if (id.equals(registryId)) {
                registerAll((Registry<MapCodec<? extends SpellFieldEffect>>) object);
            }
        });
    }

    private static void registerAll(Registry<MapCodec<? extends SpellFieldEffect>> registry) {
        if (registered || registry == null) {
            return;
        }
        registered = true;

        for (SpellFieldEffects effect : SpellFieldEffects.values()) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, effect.getSerializedName());
            if (!registry.containsKey(id)) {
                Registry.register(registry, id, effect.codec());
            }
        }

        Constants.LOG.info("Registered Enchanting Overhauled spell field effects.");
    }
}