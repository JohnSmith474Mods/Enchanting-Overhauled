package johnsmith.enchantingoverhauled.api.config.io;

import johnsmith.enchantingoverhauled.api.config.ConfigManager;
import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.data.PropertyGroup;
import johnsmith.enchantingoverhauled.api.config.data.PropertyTab;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the procedural generation of the configuration file content.
 * <p>
 * This class traverses the hierarchical structure registered in the {@link ConfigManager}
 * (Tabs -> Groups -> Properties) and serializes them into a flat properties format,
 * injecting headers and comments for readability in the output file.
 */
public class ConfigProvider {

    /**
     * The manager providing the structure and runtime values of the configuration.
     */
    private final ConfigManager manager;

    /**
     * An internal ordered list of elements (headers, comments, and config entries) to be written to the file.
     */
    private final List<IConfigElement> elements = new ArrayList<>();

    // region Factory
    /**
     * Factory method to create a new {@code ConfigProvider} instance.
     *
     * @param manager The manager containing the config structure.
     * @return A new {@code ConfigProvider} instance.
     */
    public static ConfigProvider create(ConfigManager manager) {
        return new ConfigProvider(manager);
    }
    // endregion

    /**
     * Constructs a new config provider associated with the given manager.
     *
     * @param manager The manager containing the config structure.
     */
    public ConfigProvider(ConfigManager manager) {
        this.manager = manager;
    }

    /**
     * Iterates over the Manager's structure registry and builds the internal list of file elements.
     * <p>
     * Empty tabs or groups that contain no active properties are automatically excluded.
     */
    public void build() {
        for (PropertyTab tab : manager.getTabs()) {
            boolean tabHasContent = false;
            List<IConfigElement> tabElements = new ArrayList<>();

            tabElements.add(new TabHeader(tab.id()));

            for (PropertyGroup group : manager.getGroups(tab)) {
                boolean groupHasContent = false;
                List<IConfigElement> groupElements = new ArrayList<>();

                groupElements.add(new GroupHeader(group.id()));

                for (Property<?> config : manager.getConfigs(group)) {
                    groupElements.add(new ConfigEntry(config));
                    groupHasContent = true;
                    tabHasContent = true;
                }

                if (groupHasContent) {
                    elements.addAll(groupElements);
                }
            }

            if (tabHasContent) {
                elements.addAll(tabElements);
            }
        }
    }

    /**
     * Adds a raw line of text (e.g., a comment or separator) to the output, followed by a newline.
     *
     * @param line The string content to add.
     */
    public void addRawLine(String line) {
        elements.add(new RawLine(line));
    }

    /**
     * Adds a raw key-value entry (unmanaged by the {@code ConfigManager}) to the output.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void addRawEntry(String key, String value) {
        elements.add(new RawEntry(key, value));
    }

    /**
     * Generates the final content string for the configuration file, prepended with a header.
     *
     * @param modName The display name of the mod for the file header.
     * @return The complete configuration file content as a single string.
     */
    public String generate(String modName) {
        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(modName).append(" Configuration\n");

        for (IConfigElement element : elements) {
            builder.append(element.toConfigString());
        }
        return builder.toString();
    }

    // region Internal Records

    /**
     * Internal interface representing any element that can be converted into a configuration file string.
     */
    private interface IConfigElement {
        /**
         * Converts the element into its string representation for the configuration file.
         *
         * @return The formatted configuration string.
         */
        String toConfigString();
    }

    /**
     * Represents a header for a configuration tab (e.g., "# [ENCHANTMENTS]").
     *
     * @param name The ID of the tab.
     */
    private record TabHeader(String name) implements IConfigElement {
        @Override public String toConfigString() { return "\n\n# [" + name.toUpperCase() + "]\n"; }
    }

    /**
     * Represents a header for a configuration group (e.g., "# [GENERAL]").
     *
     * @param name The ID of the group.
     */
    private record GroupHeader(String name) implements IConfigElement {
        @Override public String toConfigString() { return "\n# [" + name.toUpperCase() + "]\n"; }
    }

    /**
     * Represents a standard configuration entry, including its description as a comment.
     *
     * @param type The {@link Property} instance to serialize.
     */
    private record ConfigEntry(Property<?> type) implements IConfigElement {
        @Override public String toConfigString() {
            String description = type.description.replace("\n", "\n# ");
            return "# " + description + "\n" + type.getUniqueId() + "=" + type.get() + "\n";
        }
    }

    /**
     * Represents a raw line to be outputted directly, typically a comment.
     *
     * @param line The raw line content.
     */
    private record RawLine(String line) implements IConfigElement {
        @Override public String toConfigString() { return line + "\n"; }
    }

    /**
     * Represents a raw, unmanaged key-value entry (e.g., for preserving old config options).
     *
     * @param key The key of the entry.
     * @param value The value of the entry.
     */
    private record RawEntry(String key, String value) implements IConfigElement {
        @Override public String toConfigString() { return key + "=" + value + "\n"; }
    }
    // endregion
}