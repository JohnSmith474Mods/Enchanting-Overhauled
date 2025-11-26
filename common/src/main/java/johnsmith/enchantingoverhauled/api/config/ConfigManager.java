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
 * The central hub for the mod's configuration system.
 * <p>
 * This manager acts as a registry for configuration structure (Tabs, Groups, Properties)
 * and handles the direct loading and saving of the single configuration file.
 * <p>
 * Network synchronization and multi-file scopes have been removed.
 * All properties are stored in 'config/[mod_id].properties'.
 */
public class ConfigManager {

    // region Fields
    public final String modId;
    public final String modName;
    public final Logger logger;

    private final Map<String, Property<?>> registeredConfigs = new HashMap<>();
    private final Map<String, PropertyTab> tabs = new LinkedHashMap<>();
    private final Map<PropertyTab, List<PropertyGroup>> tabsToGroups = new LinkedHashMap<>();
    private final Map<PropertyGroup, List<Property<?>>> groupsToConfigs = new LinkedHashMap<>();

    // I/O State
    private Path configPath;
    private ConfigWriter activeConfig = new ConfigWriter(); // Default empty to avoid null checks
    // endregion

    // region Constructor
    public ConfigManager(String modId, String modName, Logger logger) {
        if (logger == null) throw new IllegalArgumentException("Logger cannot be null");
        this.logger = logger;

        if (modId == null || modId.isEmpty()) {
            logError("Mod ID cannot be null or empty.");
            throw new IllegalArgumentException("Mod ID cannot be null or empty");
        }
        // Fallback for modName if null
        this.modName = (modName == null || modName.isEmpty()) ? modId : modName;
        this.modId = modId;

        ConfigRegistry.registerManager(this);

        logInfo("Initialized ConfigManager for mod '{}'.", this.modName);
    }
    // endregion

    // region Lifecycle API

    /**
     * Initializes the configuration system by loading the main properties file.
     *
     * @param configDir The game's main config directory (e.g., .minecraft/config)
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

        // Apply loaded values to any properties already registered
        for (Property<?> property : registeredConfigs.values()) {
            applySavedValue(property);
        }
    }
    // endregion

    // region I/O Operations

    /**
     * Saves the current configuration to disk asynchronously.
     */
    public CompletableFuture<Void> save() {
        if (configPath == null) {
            logWarn("Attempted to save config but no path is registered. Call initialize() first.");
            return CompletableFuture.completedFuture(null);
        }

        logInfo("Saving configuration to: {}", configPath);

        // 1. Create the Provider.
        ConfigProvider provider = ConfigProvider.create(this);

        // 2. Build content on the main thread to ensure data consistency
        provider.build();

        // Preserve orphan values (keys in file but not in registry)
        appendOrphans(provider);

        String content = provider.generate(modName);

        // 3. Write to disk asynchronously
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

    public Map<String, String> getConfigMap() {
        return activeConfig.getRawMap();
    }
    // endregion

    // region Registry API
    public PropertyTab registerTab(String id, String translationKey) {
        if (id == null || translationKey == null) throw new IllegalArgumentException("Tab ID/Key cannot be null");
        return tabs.computeIfAbsent(id, k -> {
            PropertyTab tab = new PropertyTab(id, translationKey, this);
            tabsToGroups.put(tab, new ArrayList<>());
            return tab;
        });
    }

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

    public <T extends Comparable<T>> void registerConfig(Property<T> config) {
        if (config == null) return;
        PropertyGroup group = config.parentGroup;
        if (group == null || !groupsToConfigs.containsKey(group)) {
            logError("Cannot register config '{}': Parent group is invalid.", config.getUniqueId());
            return;
        }

        groupsToConfigs.get(group).add(config);
        registeredConfigs.put(config.getUniqueId(), config);

        // Apply value if the config file was loaded before registration
        applySavedValue(config);
    }
    // endregion

    // region Internal Helpers
    private <T extends Comparable<T>> void applySavedValue(Property<T> type) {
        String key = type.getUniqueId();
        if (activeConfig != null && activeConfig.has(key)) {
            parseAndSet(type, activeConfig.get(key));
        }
    }

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

    private void appendOrphans(ConfigProvider provider) {
        // If the active config has keys that are NOT in our registry,
        // we write them back to the file to prevent data loss (e.g. removed features or typos)
        for (String key : activeConfig.getKeys()) {
            if (!registeredConfigs.containsKey(key)) {
                provider.addRawLine("\n# [PRESERVED] " + key);
                provider.addRawEntry(key, activeConfig.get(key));
            }
        }
    }
    // endregion

    // region Accessors & Logging
    public void logInfo(String message, Object... params) { logger.info(message, params); }
    public void logWarn(String message, Object... params) { logger.warn(message, params); }
    public void logError(String message, Object... params) { logger.error(message, params); }
    public void logDebug(String message, Object... params) { logger.debug(message, params); }

    public Set<PropertyTab> getTabs() { return Collections.unmodifiableSet(tabsToGroups.keySet()); }
    public List<PropertyGroup> getGroups(PropertyTab tab) { return tabsToGroups.getOrDefault(tab, Collections.emptyList()); }
    public List<Property<?>> getConfigs(PropertyGroup group) { return groupsToConfigs.getOrDefault(group, Collections.emptyList()); }
    public Property<?> getConfig(String key) { return registeredConfigs.get(key); }
    public Collection<Property<?>> getConfigs() { return registeredConfigs.values(); }

    @SuppressWarnings("unchecked")
    public <S> S createScreen(S parent) {
        return (S) new ConfigScreen((Screen) parent, this);
    }
    // endregion
}