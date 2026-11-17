package johnsmith.enchantingoverhauled.api.config.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Utility class responsible for reading configuration data from a file
 * and providing simplified methods for accessing and setting values by key.
 * <p>
 * This class reads a flat properties file format, ignoring comments (lines starting with '#')
 * and empty lines during loading. It is designed to be the I/O layer between the file system
 * and the {@link johnsmith.enchantingoverhauled.api.config.ConfigManager}.
 */
public class ConfigWriter {
    /**
     * The internal map storing configuration keys and their raw string values as loaded from the file.
     */
    private final Map<String, String> config = new HashMap<>();

    /**
     * Constructs a new empty {@code ConfigWriter}.
     */
    public ConfigWriter() {}

    /**
     * Reads a configuration file from the specified path and loads its contents into a new instance.
     * <p>
     * Lines starting with '#' or blank lines are ignored. Key-value pairs are split by the first '='.
     *
     * @param path The file path to the configuration file.
     * @return A new {@code ConfigWriter} instance populated with file data, or an empty instance if the file does not exist or fails to load.
     */
    public static ConfigWriter fromFile(Path path) {
        ConfigWriter instance = new ConfigWriter();
        if (!Files.exists(path)) return instance;

        try (Scanner scanner = new Scanner(path)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    instance.config.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * Retrieves a copy of the raw configuration map.
     *
     * @return A map containing all stored keys and their raw string values.
     */
    public Map<String, String> getRawMap() {
        return new HashMap<>(this.config);
    }

    /**
     * Retrieves the raw string value associated with the given key.
     *
     * @param key The key to look up.
     * @return The raw string value, or {@code null} if the key is not found.
     */
    public String get(String key) {
        return config.get(key);
    }

    /**
     * Retrieves the raw string value associated with the given key, or returns a default value if the key is not found.
     *
     * @param key The key to look up.
     * @param def The default string value to return if the key is absent.
     * @return The string value or the default value.
     */
    public String getOrDefault(String key, String def) {
        return config.getOrDefault(key, def);
    }

    /**
     * Retrieves the integer value associated with the given key.
     *
     * @param key The key to look up.
     * @param def The default integer value to return if the key is absent or the value cannot be parsed.
     * @return The parsed integer value or the default value.
     */
    public int getOrDefault(String key, int def) {
        try {
            return Integer.parseInt(config.get(key));
        } catch (NumberFormatException | NullPointerException e) {
            return def;
        }
    }

    /**
     * Retrieves the boolean value associated with the given key.
     *
     * @param key The key to look up.
     * @param def The default boolean value to return if the key is absent or the value cannot be parsed.
     * @return The parsed boolean value or the default value.
     */
    public boolean getOrDefault(String key, boolean def) {
        String val = config.get(key);
        if (val != null) return Boolean.parseBoolean(val);
        return def;
    }

    /**
     * Retrieves the double-precision floating point value associated with the given key.
     *
     * @param key The key to look up.
     * @param def The default double value to return if the key is absent or the value cannot be parsed.
     * @return The parsed double value or the default value.
     */
    public double getOrDefault(String key, double def) {
        try {
            return Double.parseDouble(config.get(key));
        } catch (NumberFormatException | NullPointerException e) {
            return def;
        }
    }

    /**
     * Sets or updates the string value for a given configuration key in memory.
     *
     * @param key The configuration key.
     * @param value The string value to store.
     */
    public void set(String key, String value) {
        config.put(key, value);
    }

    /**
     * Checks if the configuration map contains the specified key.
     *
     * @param key The key to check for existence.
     * @return {@code true} if the key is present, {@code false} otherwise.
     */
    public boolean has(String key) {
        return config.containsKey(key);
    }

    /**
     * Retrieves the set of all configuration keys currently stored.
     *
     * @return A {@code Set} of configuration keys.
     */
    public Set<String> getKeys() {
        return config.keySet();
    }

    /**
     * Clears all key-value pairs from the internal configuration map.
     */
    public void clear() {
        config.clear();
    }
}