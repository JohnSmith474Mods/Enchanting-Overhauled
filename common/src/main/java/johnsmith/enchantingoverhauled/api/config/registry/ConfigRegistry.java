package johnsmith.enchantingoverhauled.api.config.registry;

import johnsmith.enchantingoverhauled.api.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("Config Overhauled");
    private static final Map<String, ConfigManager> MANAGERS = new ConcurrentHashMap<>();

    /**
     * Registers a ConfigManager instance to the global registry.
     * Called automatically by the ConfigManager constructor.
     *
     * @param manager The manager instance to register.
     */
    public static void registerManager(ConfigManager manager) {
        if (manager == null || manager.modId == null) {
            LOGGER.error("Attempted to register invalid ConfigManager.");
            return;
        }

        if (MANAGERS.containsKey(manager.modId)) {
            LOGGER.warn("Overwriting existing ConfigManager for mod '{}'. This is unexpected.", manager.modId);
        }

        MANAGERS.put(manager.modId, manager);
    }

    /**
     * Retrieves the ConfigManager for a specific mod ID.
     * Used by ConfigMetadata to resolve the manager for property registration.
     *
     * @param modId The mod ID to look up.
     * @return The ConfigManager instance, or null if not found.
     */
    public static ConfigManager getManager(String modId) {
        return MANAGERS.get(modId);
    }
}