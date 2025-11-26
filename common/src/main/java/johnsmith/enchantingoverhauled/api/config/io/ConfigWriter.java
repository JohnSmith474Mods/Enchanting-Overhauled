package johnsmith.enchantingoverhauled.api.config.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ConfigWriter {
    private final Map<String, String> config = new HashMap<>();

    public ConfigWriter() {}

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

    public Map<String, String> getRawMap() {
        return new HashMap<>(this.config);
    }

    public String get(String key) {
        return config.get(key);
    }

    public String getOrDefault(String key, String def) {
        return config.getOrDefault(key, def);
    }

    public int getOrDefault(String key, int def) {
        try {
            return Integer.parseInt(config.get(key));
        } catch (NumberFormatException | NullPointerException e) {
            return def;
        }
    }

    public boolean getOrDefault(String key, boolean def) {
        String val = config.get(key);
        if (val != null) return Boolean.parseBoolean(val);
        return def;
    }

    public double getOrDefault(String key, double def) {
        try {
            return Double.parseDouble(config.get(key));
        } catch (NumberFormatException | NullPointerException e) {
            return def;
        }
    }

    public void set(String key, String value) {
        config.put(key, value);
    }

    public boolean has(String key) {
        return config.containsKey(key);
    }

    public Set<String> getKeys() {
        return config.keySet();
    }

    public void clear() {
        config.clear();
    }
}