package johnsmith.enchantingoverhauled.api.config.data;

import johnsmith.enchantingoverhauled.api.config.ConfigManager;

import java.util.ArrayList;

public record PropertyTab(String id, String translationKey, ConfigManager manager) {
    // e.g., "Enchantments", "Anvil", "Accessibility"
    @Override
    public String translationKey() {
        return translationKey + "." + id;
    }

    public PropertyGroup registerGroup(String id) {
        return manager.registerGroup(this, id);
    }
}