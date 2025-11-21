package johnsmith.enchantingoverhauled.config;

import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A configuration provider for the Fabric platform that handles the structure and generation
 * of the configuration file.
 * <p>
 * Unlike a simple map, this provider maintains an ordered list of sections and entries,
 * allowing it to generate a configuration file that includes comments, type information,
 * and default values, rather than just key-value pairs.
 */
public class ConfigProvider implements SimpleConfig.DefaultConfig {

    /**
     * An ordered list of elements (sections and entries) that make up the configuration file structure.
     */
    private final List<IConfigElement> elements = new ArrayList<>();

    /**
     * Adds a section header to the configuration structure.
     *
     * @param name The name of the section (e.g., "Anvil Settings").
     */
    public void addSection(String name) {
        elements.add(new Section(name));
    }

    /**
     * Adds a configuration entry to the structure.
     *
     * @param keyValuePair A pair containing the config key and its default value.
     * @param description An array of strings describing the config option.
     * @param bounds A string describing the valid range or bounds of the value (optional).
     */
    public void addEntry(Pair<String, ?> keyValuePair, String[] description, String bounds) {
        elements.add(new Entry(keyValuePair.getFirst(), keyValuePair.getSecond(), description, bounds));
    }

    /**
     * Generates the default configuration content.
     * <p>
     * This method is called by {@link SimpleConfig} when the config file is missing.
     * It generates the content using the registered default values.
     *
     * @param namespace The namespace (usually the mod ID), unused in this implementation.
     * @return The full string content of the default configuration file.
     */
    @Override
    public String get(String namespace) {
        // Generate with no overrides (uses defaults)
        return generate(Map.of());
    }

    /**
     * Generates the configuration file content using a map of current values.
     * <p>
     * This method is used when saving the configuration from the in-game GUI. It reconstructs
     * the file string, preserving comments and structure, while inserting the provided
     * runtime values instead of the defaults.
     *
     * @param currentValues A map of config keys to their current runtime values.
     * @return The full string content of the configuration file.
     */
    public String generate(Map<String, Object> currentValues) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Enchanting Overhauled Configuration\n");

        for (IConfigElement element : elements) {
            builder.append(element.toConfigString(currentValues));
        }
        return builder.toString();
    }

    // --- Helper Classes ---

    /**
     * Interface representing a structural element in the configuration file (e.g., a header or an entry).
     */
    private interface IConfigElement {
        /**
         * Converts the element into its string representation for the config file.
         *
         * @param values A map of current values to use for overrides.
         * @return The formatted string.
         */
        String toConfigString(Map<String, Object> values);
    }

    /**
     * Represents a section header in the configuration file.
     */
    private record Section(String name) implements IConfigElement {
        @Override
        public String toConfigString(Map<String, Object> values) {
            return "\n# [" + name.toUpperCase() + "]\n\n";
        }
    }

    /**
     * Represents a single configuration option entry.
     */
    private record Entry(String key, Object defaultValue, String[] description, String bounds) implements IConfigElement {
        @Override
        public String toConfigString(Map<String, Object> values) {
            StringBuilder sb = new StringBuilder();

            // 1. Comments
            for (String line : description) {
                sb.append("# ").append(line).append("\n");
            }

            // 2. Metadata (Type, Default, Bounds)
            String type = defaultValue.getClass().getSimpleName();
            sb.append("# Type: ").append(type)
                    .append(", Default: ").append(defaultValue);

            if (bounds != null && !bounds.isEmpty()) {
                sb.append(", ").append(bounds);
            }
            sb.append("\n");

            // 3. Key = Value
            // Check if there is an override value, otherwise use default
            Object val = values.getOrDefault(key, defaultValue);
            sb.append(key).append("=").append(val).append("\n\n");

            return sb.toString();
        }
    }
}