package johnsmith.enchantingoverhauled.client.gui;

import johnsmith.enchantingoverhauled.api.config.client.gui.ConfigScreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import johnsmith.enchantingoverhauled.config.Config;

/**
 * Integration entry point for the Mod Menu library on Fabric.
 * <p>
 * This class implements {@link ModMenuApi} to register the mod's custom configuration screen factory.
 * It allows players to open the {@link ConfigScreen}
 * directly from the in-game mod list UI.
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Config.MANAGER::createScreen;
    }
}