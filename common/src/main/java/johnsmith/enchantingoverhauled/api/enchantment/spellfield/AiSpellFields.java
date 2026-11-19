package johnsmith.enchantingoverhauled.api.enchantment.spellfield;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

/**
 * Holds the static {@link ResourceKey} reference for the custom data-driven
 * {@link AiSpellField} registry.
 */
public final class AiSpellFields {

    /**
     * The {@link ResourceKey} identifying the dynamic registry containing all {@link AiSpellField} definitions.
     */
    public static final ResourceKey<Registry<AiSpellField>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("ai_spell_field"));

    /**
     * Private constructor to prevent instantiation.
     */
    private AiSpellFields() {}

    /**
     * Triggers static class initialization to ensure the registry key is created.
     */
    public static void initialize() {}
}