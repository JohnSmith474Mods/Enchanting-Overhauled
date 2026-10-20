package johnsmith.enchantingoverhauled.api.loot;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Holds the static {@link ResourceKey} reference for the custom data-driven
 * {@link LootInjection} registry.
 */
public class LootInjections {

    /**
     * The {@link ResourceKey} identifying the dynamic registry containing all {@link LootInjection} definitions.
     */
    public static final ResourceKey<Registry<LootInjection>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("loot_injection"));
}