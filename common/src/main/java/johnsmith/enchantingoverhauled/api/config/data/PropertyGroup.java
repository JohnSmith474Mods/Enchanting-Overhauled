package johnsmith.enchantingoverhauled.api.config.data;

import johnsmith.enchantingoverhauled.api.config.ConfigManager;

/**
 * A record representing a logical group of related configuration properties within a single {@link PropertyTab}.
 * <p>
 * This class is used to structure the configuration for organization in the GUI and file output.
 * Examples of groups are "General", "Anvil", or "Protection Calculation" within a larger tab like "Enchantment".
 *
 * @param parent         The parent tab that contains this group.
 * @param id             The unique, non-localized identifier for this group (e.g., "general").
 * @param translationKey The fully qualified translation key for the group's title in the GUI.
 * @param manager        The central configuration manager responsible for registering and managing all properties.
 */
public record PropertyGroup(PropertyTab parent, String id, String translationKey, ConfigManager manager) {

    /**
     * Generates the fully qualified translation key for a property group.
     * The format is inherited from the parent tab, followed by the group ID (e.g., "config.modid.tabid.groupid").
     *
     * @param tab     The parent tab.
     * @param groupId The unique identifier for the group.
     * @return The combined translation key string.
     */
    public static String createTranslationKey(PropertyTab tab, String groupId) {
        return tab.translationKey() + "." + groupId;
    }

    /**
     * Registers a new configuration property under this group with the central {@link ConfigManager}.
     *
     * @param config The property instance to register.
     * @param <T>    The data type of the property value.
     * @param <C>    The concrete class of the property.
     * @return The registered property instance (for fluent chaining).
     */
    public <T extends Comparable<T>, C extends Property<T>> C register(C config) {
        this.manager.registerConfig(config);
        return config;
    }
}