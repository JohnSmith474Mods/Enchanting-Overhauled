package johnsmith.enchantingoverhauled.platform.enchantment.theme;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

// This class LIVES IN THE NEOFORGE PROJECT
public class EnchantmentThemeAssignmentLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final String DATA_PATH = "enchantment_theme_assignments";

    public EnchantmentThemeAssignmentLoader() {
        super(GSON, DATA_PATH);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
        // Delegate all logic to the common manager
        EnchantmentThemeManager.apply(prepared);
    }
}