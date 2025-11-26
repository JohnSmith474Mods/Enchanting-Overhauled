package johnsmith.enchantingoverhauled.api.config.io;

import johnsmith.enchantingoverhauled.api.config.ConfigManager;
import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.data.PropertyGroup;
import johnsmith.enchantingoverhauled.api.config.data.PropertyTab;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the generation of the configuration file content.
 * <p>
 * Iterates through the ConfigManager's registered structure (Tabs -> Groups -> Properties)
 * and serializes them into a flat properties format with headers and comments.
 */
public class ConfigProvider {

    private final ConfigManager manager;
    private final List<IConfigElement> elements = new ArrayList<>();

    // region Factory
    /**
     * Creates a new ConfigProvider for the given manager.
     *
     * @param manager The manager containing the config structure.
     * @return A new ConfigProvider instance.
     */
    public static ConfigProvider create(ConfigManager manager) {
        return new ConfigProvider(manager);
    }
    // endregion

    public ConfigProvider(ConfigManager manager) {
        this.manager = manager;
    }

    /**
     * Iterates over the Manager's registry and builds the list of file elements.
     */
    public void build() {
        for (PropertyTab tab : manager.getTabs()) {
            boolean tabHasContent = false;
            List<IConfigElement> tabElements = new ArrayList<>();

            // Add Tab Header (e.g. # [ENCHANTMENTS])
            tabElements.add(new TabHeader(tab.id()));

            for (PropertyGroup group : manager.getGroups(tab)) {
                boolean groupHasContent = false;
                List<IConfigElement> groupElements = new ArrayList<>();

                // Add Group Header (e.g. # [GENERAL])
                groupElements.add(new GroupHeader(group.id()));

                for (Property<?> config : manager.getConfigs(group)) {
                    // Add every config in this group (No scope filtering)
                    groupElements.add(new ConfigEntry(config));
                    groupHasContent = true;
                    tabHasContent = true;
                }

                // Only add the group header if it actually has configs
                if (groupHasContent) {
                    tabElements.addAll(groupElements);
                }
            }

            // Only add the tab header if it actually has groups with configs
            if (tabHasContent) {
                elements.addAll(tabElements);
            }
        }
    }

    public void addRawLine(String line) {
        elements.add(new RawLine(line));
    }

    public void addRawEntry(String key, String value) {
        elements.add(new RawEntry(key, value));
    }

    public String generate(String modName) {
        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(modName).append(" Configuration\n");

        for (IConfigElement element : elements) {
            builder.append(element.toConfigString());
        }
        return builder.toString();
    }

    // region Internal Records
    private interface IConfigElement { String toConfigString(); }

    private record TabHeader(String name) implements IConfigElement {
        @Override public String toConfigString() { return "\n\n# [" + name.toUpperCase() + "]\n"; }
    }

    private record GroupHeader(String name) implements IConfigElement {
        @Override public String toConfigString() { return "\n# [" + name.toUpperCase() + "]\n"; }
    }

    private record ConfigEntry(Property<?> type) implements IConfigElement {
        @Override public String toConfigString() {
            // Indent description lines for readability
            String description = type.description.replace("\n", "\n# ");
            return "# " + description + "\n" + type.getUniqueId() + "=" + type.get() + "\n";
        }
    }

    private record RawLine(String line) implements IConfigElement {
        @Override public String toConfigString() { return line + "\n"; }
    }

    private record RawEntry(String key, String value) implements IConfigElement {
        @Override public String toConfigString() { return key + "=" + value + "\n"; }
    }
    // endregion
}