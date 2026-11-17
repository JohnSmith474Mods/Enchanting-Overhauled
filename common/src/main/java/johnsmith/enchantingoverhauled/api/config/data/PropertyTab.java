package johnsmith.enchantingoverhauled.api.config.data;

import johnsmith.enchantingoverhauled.api.config.ConfigManager;

/**
 * A record representing a top-level category or section of configuration options in the GUI.
 * <p>
 * This class serves as the root container for {@link PropertyGroup}s within the UI and is used
 * to generate the tabs in the {@code ConfigScreen}.
 *
 * @param id             The unique, non-localized identifier for this tab (e.g., "enchantment", "general").
 * @param translationKey The base translation key string shared by the mod (e.g., "config.mod_id").
 * @param manager        The central configuration manager responsible for handling the overall config structure.
 */
public record PropertyTab(String id, String translationKey, ConfigManager manager) {

    /**
     * Overrides the default accessor to provide the full, qualified translation key for the tab.
     * <p>
     * The format combines the base key and the tab's ID (e.g., "config.mod_id.enchantment").
     *
     * @return The fully qualified translation key string.
     */
    @Override
    public String translationKey() {
        return translationKey + "." + id;
    }

    /**
     * Registers a new {@link PropertyGroup} as a child of this tab.
     * <p>
     * This delegates the registration process to the central {@link ConfigManager}.
     *
     * @param id The unique identifier for the new group (e.g., "general", "anvil").
     * @return The newly created or retrieved {@link PropertyGroup}.
     */
    public PropertyGroup registerGroup(String id) {
        return manager.registerGroup(this, id);
    }
}