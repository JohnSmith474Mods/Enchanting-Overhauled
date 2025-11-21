package johnsmith.enchantingoverhauled.platform.enchantment.theme;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class EnchantmentThemeAssignmentLoader extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final String DATA_PATH = "enchantment_theme_assignments";
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "enchantment_theme_assignment_loader");

    public EnchantmentThemeAssignmentLoader() {
        super(GSON, DATA_PATH);
    }

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
        EnchantmentThemeManager.apply(prepared);
    }
}