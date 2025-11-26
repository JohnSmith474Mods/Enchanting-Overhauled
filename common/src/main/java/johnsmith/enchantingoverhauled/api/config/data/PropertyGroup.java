package johnsmith.enchantingoverhauled.api.config.data;

import johnsmith.enchantingoverhauled.api.config.ConfigManager;

public record PropertyGroup(PropertyTab parent, String id, String translationKey, ConfigManager manager) {
    // e.g., "Protection", "Damage", "General"
    public static String createTranslationKey(PropertyTab tab, String groupId) {
        return tab.translationKey() + "." + groupId;
    }

    public <T extends Comparable<T>, C extends Property<T>> C register(C config) {
        this.manager.registerConfig(config);
        return config;
    }
}