package johnsmith.enchantingoverhauled.api.config.data;

import johnsmith.enchantingoverhauled.api.config.ConfigManager;
import johnsmith.enchantingoverhauled.api.config.registry.ConfigRegistry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A record used for external configuration lookups, such as in data-driven values.
 * <p>
 * This structure holds the necessary identifiers to uniquely locate a specific
 * {@link Property} within a {@link ConfigManager}'s hierarchical structure (Mod -> Tab -> Group -> Property).
 *
 * @param modId        The unique identifier of the mod owning the configuration (e.g., "enchanting_overhauled").
 * @param tabId        The ID of the top-level configuration tab (e.g., "enchantment").
 * @param groupId      The ID of the property group within the tab (e.g., "general").
 * @param resourceName The simple resource name of the configuration property (e.g., "enchantment_max_level").
 */
public record ConfigMetadata(
        String modId,
        String tabId,
        String groupId,
        String resourceName
) {
    /**
     * The codec responsible for serializing and deserializing this record from data files (e.g., JSON).
     */
    public static final Codec<ConfigMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("mod_id").forGetter(ConfigMetadata::modId),
            Codec.STRING.fieldOf("tab_id").forGetter(ConfigMetadata::tabId),
            Codec.STRING.fieldOf("group_id").forGetter(ConfigMetadata::groupId),
            Codec.STRING.fieldOf("resource_name").forGetter(ConfigMetadata::resourceName)
    ).apply(instance, ConfigMetadata::new));

    /**
     * Generates the unique, internal ID string used to look up this config within its {@link ConfigManager}.
     * The format is {@code tabId.groupId.resourceName}.
     *
     * @return The unique identifier string.
     */
    public String getUniqueId() {
        return tabId + "." + groupId + "." + resourceName;
    }

    /**
     * Attempts to retrieve or register the {@link PropertyTab} associated with this metadata's IDs.
     *
     * @return The retrieved or newly created {@link PropertyTab}, or {@code null} if the {@link ConfigManager} is unavailable.
     */
    public PropertyTab getOrCreateTab() {
        ConfigManager manager = getManager();
        if (manager == null) return null;

        return manager.registerTab(tabId, "config." + modId);
    }

    /**
     * Attempts to retrieve or register the {@link PropertyGroup} associated with this metadata's IDs.
     * This operation implicitly attempts to create the parent {@link PropertyTab} if it does not exist.
     *
     * @return The retrieved or newly created {@link PropertyGroup}, or {@code null} if the {@link ConfigManager} is unavailable.
     */
    public PropertyGroup getOrCreateGroup() {
        ConfigManager manager = getManager();
        if (manager == null) return null;

        PropertyTab tab = getOrCreateTab();
        return manager.registerGroup(tab, groupId);
    }

    /**
     * Retrieves the {@link ConfigManager} instance registered under this metadata's {@code modId}.
     *
     * @return The associated {@link ConfigManager}, or {@code null} if none is found.
     */
    public ConfigManager getManager() {
        return ConfigRegistry.getManager(modId);
    }
}