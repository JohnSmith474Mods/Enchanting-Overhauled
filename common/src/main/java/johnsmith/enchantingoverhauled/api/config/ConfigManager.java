package johnsmith.enchantingoverhauled.api.config;

import johnsmith.enchantingoverhauled.api.config.client.gui.ConfigScreen;
import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.data.PropertyGroup;
import johnsmith.enchantingoverhauled.api.config.data.PropertyTab;
import johnsmith.enchantingoverhauled.api.config.io.ConfigProvider;
import johnsmith.enchantingoverhauled.api.config.io.ConfigWriter;
import johnsmith.enchantingoverhauled.api.config.registry.ConfigRegistry;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * The central manager for the mod's configuration system.
 * <p>
 * This class acts as both the registry for the configuration structure (Tabs -> Groups -> Properties)
 * and the handler for file I/O. It maintains a single flat properties file located at
 * {@code config/[mod_id].properties}.
 * <p>
 * It supports asynchronous saving to prevent game thread stalling and maintains a runtime
 * map of registered properties to their current values.
 */
public class ConfigManager {

    /**
     * The unique identifier for the mod (e.g., "enchanting_overhauled").
     */
    public final String modId;

    /**
     * The display name of the mod, used in file headers and logging.
     */
    public final String modName;

    /**
     * The logger instance for reporting configuration events and errors.
     */
    public final Logger logger;

    /**
     * A lookup map for all registered properties, keyed by their unique ID (tab.group.name).
     */
    private final Map<String, Property<?>> registeredConfigs = new HashMap<>();

    /**
     * A map of registered configuration tabs, keyed by their ID (e.g., "general", "client").
     * Used to ensure unique tab IDs and quick retrieval.
     */
    private final Map<String, PropertyTab> tabs = new LinkedHashMap<>();

    /**
     * A mapping of Tabs to their child PropertyGroups.
     * Maintains the order in which groups were registered for UI display.
     */
    private final Map<PropertyTab, List<PropertyGroup>> tabsToGroups = new LinkedHashMap<>();

    /**
     * A mapping of PropertyGroups to their child Properties.
     * Maintains the order in which properties were registered for UI display.
     */
    private final Map<PropertyGroup, List<Property<?>>> groupsToConfigs = new LinkedHashMap<>();

    /**
     * The file path to the configuration file on the disk.
     * Initialized during {@link #initialize(Path)}.
     */
    private Path configPath;

    /**
     * An in-memory representation of the raw configuration file content.
     * Used to persist values that exist in the file but are not currently registered in the code (orphans).
     */
    private ConfigWriter activeConfig = new ConfigWriter();

    /**
     * Constructs a new configuration manager and registers it with the global {@link ConfigRegistry}.
     *
     * @param modId   The mod's unique identifier. Cannot be null or empty.
     * @param modName The mod's display name. If null, defaults to {@code modId}.
     * @param logger  The logger to use for all configuration-related messages.
     * @throws IllegalArgumentException if {@code modId} is null/empty or {@code logger} is null.
     */
    public ConfigManager(String modId, String modName, Logger logger) {
        if (logger == null) throw new IllegalArgumentException("Logger cannot be null");
        this.logger = logger;

        if (modId == null || modId.isEmpty()) {
            logError("Mod ID cannot be null or empty.");
            throw new IllegalArgumentException("Mod ID cannot be null or empty");
        }

        this.modName = (modName == null || modName.isEmpty()) ? modId : modName;
        this.modId = modId;

        ConfigRegistry.registerManager(this);

        logInfo("Initialized ConfigManager for mod '{}'.", this.modName);
    }

    /**
     * Initializes the configuration system by determining the file path and loading existing values.
     * <p>
     * If the configuration file exists, it is parsed and values are applied to any currently
     * registered properties. If it does not exist, a new file will be created upon the first save.
     *
     * @param configDir The game's main configuration directory path.
     */
    public void initialize(Path configDir) {
        if (configDir == null) {
            logError("Failed to initialize config: directory is null.");
            return;
        }

        this.configPath = configDir.resolve(modId + ".properties");
        logInfo("Loading configuration from: {}", this.configPath);

        if (Files.exists(this.configPath)) {
            this.activeConfig = ConfigWriter.fromFile(this.configPath);
        } else {
            logInfo("Config file not found. A new file will be created on save.");
            this.activeConfig = new ConfigWriter();
        }

        for (Property<?> property : registeredConfigs.values()) {
            applySavedValue(property);
        }
    }

    /**
     * Asynchronously saves the current configuration state to disk.
     * <p>
     * This method generates the file content using {@link ConfigProvider}, preserving any
     * orphan keys found in the file that are not currently registered in the code.
     * The write operation happens on a separate thread to avoid blocking the main game loop.
     *
     * @return A {@link CompletableFuture} representing the completion of the save operation.
     */
    public CompletableFuture<Void> save() {
        if (configPath == null) {
            logWarn("Attempted to save config but no path is registered. Call initialize() first.");
            return CompletableFuture.completedFuture(null);
        }

        logInfo("Saving configuration to: {}", configPath);

        ConfigProvider provider = ConfigProvider.create(this);

        provider.build();

        appendOrphans(provider);

        String content = provider.generate(modName);

        return CompletableFuture.runAsync(() -> {
            try {
                Path parent = configPath.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Files.writeString(configPath, content);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            logError("Failed to save config to '{}': {}", configPath, e);
            return null;
        });
    }

    /**
     * Retrieves a raw map of the currently loaded configuration key-value pairs.
     *
     * @return A map of string keys to string values.
     */
    public Map<String, String> getConfigMap() {
        return activeConfig.getRawMap();
    }

    /**
     * Registers or retrieves a configuration tab.
     *
     * @param id             The unique identifier for the tab.
     * @param translationKey The base translation key for the tab.
     * @return The registered {@link PropertyTab}.
     */
    public PropertyTab registerTab(String id, String translationKey) {
        if (id == null || translationKey == null) throw new IllegalArgumentException("Tab ID/Key cannot be null");
        return tabs.computeIfAbsent(id, k -> {
            PropertyTab tab = new PropertyTab(id, translationKey, this);
            tabsToGroups.put(tab, new ArrayList<>());
            return tab;
        });
    }

    /**
     * Registers or retrieves a property group within a specific tab.
     *
     * @param tab The parent tab.
     * @param id  The unique identifier for the group.
     * @return The registered {@link PropertyGroup}.
     * @throws IllegalArgumentException if the tab is not registered with this manager.
     */
    public PropertyGroup registerGroup(PropertyTab tab, String id) {
        if (tab == null || id == null) throw new IllegalArgumentException("Tab/Group ID cannot be null");
        if (!tabsToGroups.containsKey(tab)) throw new IllegalArgumentException("Tab not registered: " + tab.id());

        return tabsToGroups.get(tab).stream()
                .filter(g -> g.id().equals(id))
                .findFirst()
                .orElseGet(() -> {
                    PropertyGroup group = new PropertyGroup(tab, id, PropertyGroup.createTranslationKey(tab, id), this);
                    tabsToGroups.get(tab).add(group);
                    groupsToConfigs.put(group, new ArrayList<>());
                    return group;
                });
    }

    /**
     * Registers a configuration property.
     * <p>
     * If a value for this property exists in the loaded file, it is applied immediately.
     *
     * @param config The property to register.
     * @param <T>    The type of the property value.
     */
    public <T extends Comparable<T>> void registerConfig(Property<T> config) {
        if (config == null) return;
        PropertyGroup group = config.parentGroup;
        if (group == null || !groupsToConfigs.containsKey(group)) {
            logError("Cannot register config '{}': Parent group is invalid.", config.getUniqueId());
            return;
        }

        groupsToConfigs.get(group).add(config);
        registeredConfigs.put(config.getUniqueId(), config);

        applySavedValue(config);
    }

    /**
     * Applies the value from the currently loaded {@link ConfigWriter} to the given property, if it exists.
     *
     * @param type The property to update.
     */
    private <T extends Comparable<T>> void applySavedValue(Property<T> type) {
        String key = type.getUniqueId();
        if (activeConfig != null && activeConfig.has(key)) {
            parseAndSet(type, activeConfig.get(key));
        }
    }

    /**
     * Parses a string value and sets it on the property, handling type conversion.
     *
     * @param type  The property to set.
     * @param value The string representation of the value.
     */
    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> void parseAndSet(Property<T> type, String value) {
        if (type == null || value == null) return;
        try {
            if (type instanceof Property.Binary binary) {
                binary.set(Boolean.parseBoolean(value));
            } else if (type instanceof Property.Bounded<?> bounded) {
                if (bounded.defaultValue instanceof Integer) {
                    ((Property.Bounded<Integer>) bounded).set(Integer.parseInt(value));
                } else if (bounded.defaultValue instanceof Double) {
                    ((Property.Bounded<Double>) bounded).set(Double.parseDouble(value));
                } else if (bounded.defaultValue instanceof Float) {
                    ((Property.Bounded<Float>) bounded).set(Float.parseFloat(value));
                }
            }
        } catch (Exception e) {
            logError("Failed to parse config value for '{}': {}", type.resourceName, value);
        }
    }

    /**
     * Appends configuration entries found in the file but not in the code to the output provider.
     * This ensures that removing a config option from the mod code doesn't silently delete user data
     * from the file, effectively "commenting it out" as preserved data.
     *
     * @param provider The config provider being built.
     */
    private void appendOrphans(ConfigProvider provider) {
        for (String key : activeConfig.getKeys()) {
            if (!registeredConfigs.containsKey(key)) {
                provider.addRawLine("\n# [PRESERVED] " + key);
                provider.addRawEntry(key, activeConfig.get(key));
            }
        }
    }

    /**
     * Logs an informational message prefixed with the mod ID.
     *
     * @param message The message string (can contain {} placeholders).
     * @param params  Arguments to substitute into the placeholders.
     */
    public void logInfo(String message, Object... params) { logger.info(message, params); }

    /**
     * Logs a warning message prefixed with the mod ID.
     *
     * @param message The message string (can contain {} placeholders).
     * @param params  Arguments to substitute into the placeholders.
     */
    public void logWarn(String message, Object... params) { logger.warn(message, params); }

    /**
     * Logs an error message prefixed with the mod ID.
     *
     * @param message The message string (can contain {} placeholders).
     * @param params  Arguments to substitute into the placeholders.
     */
    public void logError(String message, Object... params) { logger.error(message, params); }

    /**
     * Logs a debug message prefixed with the mod ID.
     *
     * @param message The message string (can contain {} placeholders).
     * @param params  Arguments to substitute into the placeholders.
     */
    public void logDebug(String message, Object... params) { logger.debug(message, params); }

    /**
     * Retrieves an unmodifiable set of all registered property tabs.
     *
     * @return A set of {@link PropertyTab} instances.
     */
    public Set<PropertyTab> getTabs() { return Collections.unmodifiableSet(tabsToGroups.keySet()); }

    /**
     * Retrieves the list of groups registered under a specific tab.
     *
     * @param tab The tab to query.
     * @return A list of property groups in registration order.
     */
    public List<PropertyGroup> getGroups(PropertyTab tab) { return tabsToGroups.getOrDefault(tab, Collections.emptyList()); }

    /**
     * Retrieves the list of configuration properties registered under a specific group.
     *
     * @param group The group to query.
     * @return A list of properties in registration order.
     */
    public List<Property<?>> getConfigs(PropertyGroup group) { return groupsToConfigs.getOrDefault(group, Collections.emptyList()); }

    /**
     * Retrieves a registered property by its unique fully-qualified ID string.
     *
     * @param key The unique ID (format: tab.group.name).
     * @return The property instance, or {@code null} if not found.
     */
    public Property<?> getConfig(String key) { return registeredConfigs.get(key); }

    /**
     * Retrieves a collection of all registered configuration properties.
     *
     * @return A collection of all properties managed by this instance.
     */
    public Collection<Property<?>> getConfigs() { return registeredConfigs.values(); }

    /**
     * Creates a new instance of the configuration GUI screen.
     *
     * @param parent The parent screen to return to when the config screen is closed.
     * @param <S>    The return type, typically castable to the platform-specific screen type.
     * @return A new {@link ConfigScreen} instance.
     */
    @SuppressWarnings("unchecked")
    public <S> S createScreen(S parent) {
        return (S) new ConfigScreen((Screen) parent, this);
    }
}