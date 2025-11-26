package johnsmith.enchantingoverhauled.api.config.data;

import johnsmith.enchantingoverhauled.api.config.ConfigManager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import johnsmith.enchantingoverhauled.api.config.registry.ConfigRegistry;

public record ConfigMetadata(
        String modId,
        String tabId,
        String groupId,
        String resourceName
) {
    public static final Codec<ConfigMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("mod_id").forGetter(ConfigMetadata::modId),
            Codec.STRING.fieldOf("tab_id").forGetter(ConfigMetadata::tabId),
            Codec.STRING.fieldOf("group_id").forGetter(ConfigMetadata::groupId),
            Codec.STRING.fieldOf("resource_name").forGetter(ConfigMetadata::resourceName)
    ).apply(instance, ConfigMetadata::new));

    /**
     * Generates the unique ID string used to look up this config in the manager.
     */
    public String getUniqueId() {
        return tabId + "." + groupId + "." + resourceName;
    }

    /**
     * Helper to find the manager and ensure the Tab exists.
     */
    public PropertyTab getOrCreateTab() {
        ConfigManager manager = getManager();
        if (manager == null) return null;

        return manager.registerTab(tabId, "config." + modId);
    }

    /**
     * Helper to find the manager and ensure the Tab and Group exist.
     */
    public PropertyGroup getOrCreateGroup() {
        ConfigManager manager = getManager();
        if (manager == null) return null;

        PropertyTab tab = getOrCreateTab();
        return manager.registerGroup(tab, groupId);
    }

    public ConfigManager getManager() {
        return ConfigRegistry.getManager(modId);
    }
}