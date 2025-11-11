package johnsmith.config;

import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ConfigProvider implements SimpleConfig.DefaultConfig {
    private final StringBuilder configBuilder = new StringBuilder();
    private final List<Pair<String, ?>> configsList = new ArrayList<>();

    public List<Pair<String, ?>> getConfigsList() {
        return configsList;
    }

    /**
     * Adds a section heading to the config file.
     * @param name The name of the section.
     */
    public void addSection(String name) {
        configBuilder.append("\n# [").append(name.toUpperCase()).append("]\n\n");
    }

    /**
     * Adds a new config entry with detailed comments.
     * @param keyValuePair The Pair containing the key and default value.
     * @param description One or more lines describing the config option.
     * @param bounds A string describing the bounds (e.g., "Min: 0, Max: 1.0").
     */
    public void addEntry(Pair<String, ?> keyValuePair, String[] description, String bounds) {
        configsList.add(keyValuePair);

        // Add description comments
        for (String line : description) {
            configBuilder.append("# ").append(line).append("\n");
        }

        // Add bounds and default value comment
        String type = keyValuePair.getSecond().getClass().getSimpleName();
        configBuilder.append("# Type: ").append(type)
                .append(", Default: ").append(keyValuePair.getSecond());
        if (bounds != null && !bounds.isEmpty()) {
            configBuilder.append(", ").append(bounds);
        }
        configBuilder.append("\n");

        // Add the key-value pair
        configBuilder.append(keyValuePair.getFirst()).append("=").append(keyValuePair.getSecond()).append("\n\n");
    }

    // --- DEPRECATED METHOD ---
    // This method is kept to show the old implementation for comparison.
    // The new `addEntry` method replaces this.
    @Deprecated
    public void addKeyValuePair(Pair<String, ?> keyValuePair, String comment) {
        configsList.add(keyValuePair);
        // Old, simple implementation
        configBuilder.append(keyValuePair.getFirst()).append("=").append(keyValuePair.getSecond()).append(" #")
                .append(comment).append(" | default: ").append(keyValuePair.getSecond()).append("\n");
    }

    @Override
    public String get(String namespace) {
        return configBuilder.toString();
    }
}