package johnsmith.enchantingoverhauled.api.enchantment.theme.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.accessor.EnchantmentThemeAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.Map;

// This is NOT a reload listener. It's just a manager.
public class EnchantmentThemeManager {

    // This method contains all your platform-independent logic
    public static void apply(Map<ResourceLocation, JsonElement> prepared) {
        Constants.LOG.info("Loading enchantment theme assignments...");
        Map<ResourceLocation, ResourceLocation> themeAssignments = new HashMap<>();

        // 1. Parse all JSON files and merge them
        for (Map.Entry<ResourceLocation, JsonElement> entry : prepared.entrySet()) {
            try {
                JsonObject jsonObject = entry.getValue().getAsJsonObject();
                for (Map.Entry<String, JsonElement> assignmentEntry : jsonObject.entrySet()) {
                    ResourceLocation enchantmentId = ResourceLocation.tryParse(assignmentEntry.getKey());
                    ResourceLocation themeId = ResourceLocation.tryParse(assignmentEntry.getValue().getAsString());

                    if (enchantmentId == null) {
                        Constants.LOG.warn("Invalid enchantment ID format '{}' in {}", assignmentEntry.getKey(), entry.getKey());
                        continue;
                    }
                    if (themeId == null) {
                        Constants.LOG.warn("Invalid theme ID format '{}' for enchantment '{}' in {}", assignmentEntry.getValue().getAsString(), enchantmentId, entry.getKey());
                        continue;
                    }

                    Constants.LOG.debug("Loading theme assignment from {}: {} -> {}", entry.getKey(), enchantmentId, themeId);
                    themeAssignments.put(enchantmentId, themeId);
                }
            } catch (Exception e) {
                Constants.LOG.error("Failed to parse enchantment theme assignment file: " + entry.getKey(), e);
            }
        }

        Constants.LOG.info("Loaded {} enchantment theme assignments.", themeAssignments.size());

        // 2. Apply the loaded assignments
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            ResourceLocation id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);

            if (themeAssignments.containsKey(id)) {
                ResourceLocation themeId = themeAssignments.get(id);
                ResourceKey<EnchantmentTheme> themeKey = ResourceKey.create(EnchantmentThemeRegistry.THEME_REGISTRY_KEY, themeId);
                ((EnchantmentThemeAccessor) enchantment).enchanting_overhauled$setTheme(themeKey);
            }
        }
    }
}