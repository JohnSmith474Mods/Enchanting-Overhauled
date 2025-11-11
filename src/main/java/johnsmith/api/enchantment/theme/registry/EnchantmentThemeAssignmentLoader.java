package johnsmith.api.enchantment.theme.registry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import johnsmith.EnchantingOverhauled;
import johnsmith.api.enchantment.theme.accessor.EnchantmentThemeAccessor;
import johnsmith.api.enchantment.theme.EnchantmentTheme;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads and applies enchantment theme *assignments* from data packs.
 * This listener reads all .json files from the "data/<namespace>/enchantment_theme/enchantments/" directory.
 */
public class EnchantmentThemeAssignmentLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new Gson();
    // 1. This loader reads from the "enchantments" subdirectory.
    private static final String DATA_PATH = "enchantment_theme_assignments";
    // 2. The ID is updated to be descriptive.
    public static final Identifier ID = new Identifier(EnchantingOverhauled.MOD_ID, "enchantment_theme_assignment_loader");

    public EnchantmentThemeAssignmentLoader() {
        super(GSON, DATA_PATH);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        EnchantingOverhauled.LOGGER.info("Loading enchantment theme assignments...");
        Map<Identifier, Identifier> themeAssignments = new HashMap<>();

        // 1. Parse all JSON files and merge them
        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            try {
                // The file can be any .json file, so we load its content
                JsonObject jsonObject = entry.getValue().getAsJsonObject();
                for (Map.Entry<String, JsonElement> assignmentEntry : jsonObject.entrySet()) {
                    Identifier enchantmentId = Identifier.tryParse(assignmentEntry.getKey());
                    Identifier themeId = Identifier.tryParse(assignmentEntry.getValue().getAsString());

                    if (enchantmentId == null) {
                        EnchantingOverhauled.LOGGER.warn("Invalid enchantment ID format '{}' in {}", assignmentEntry.getKey(), entry.getKey());
                        continue;
                    }
                    if (themeId == null) {
                        EnchantingOverhauled.LOGGER.warn("Invalid theme ID format '{}' for enchantment '{}' in {}", assignmentEntry.getValue().getAsString(), enchantmentId, entry.getKey());
                        continue;
                    }

                    // We log which file the assignment came from, in case of conflicts
                    EnchantingOverhauled.LOGGER.debug("Loading theme assignment from {}: {} -> {}", entry.getKey(), enchantmentId, themeId);
                    themeAssignments.put(enchantmentId, themeId);
                }
            } catch (Exception e) {
                EnchantingOverhauled.LOGGER.error("Failed to parse enchantment theme assignment file: " + entry.getKey(), e);
            }
        }

        EnchantingOverhauled.LOGGER.info("Loaded {} enchantment theme assignments.", themeAssignments.size());

        // 2. Apply the loaded assignments
        // This runs after registries are frozen, so we can safely iterate them.
        for (Enchantment enchantment : Registries.ENCHANTMENT) {
            Identifier id = Registries.ENCHANTMENT.getId(enchantment);

            if (themeAssignments.containsKey(id)) {
                // This enchantment has a theme assigned in the JSON
                Identifier themeId = themeAssignments.get(id);

                // Create the key
                RegistryKey<EnchantmentTheme> themeKey = RegistryKey.of(EnchantmentThemeRegistry.THEME_REGISTRY_KEY, themeId);

                // Set the key. DO NOT validate it here.
                ((EnchantmentThemeAccessor) enchantment).setTheme(themeKey);

            }
            // If an enchantment is not in the map, do nothing.
            // The accessor's getter should handle the default.
        }
    }
}
