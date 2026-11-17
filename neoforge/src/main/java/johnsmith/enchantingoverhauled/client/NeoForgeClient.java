package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.client.render.entity.model.TomeModel;
import johnsmith.enchantingoverhauled.config.Config;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-side event handler for the NeoForge platform.
 * <p>
 * This class encapsulates client-specific initialization logic, such as registering model layer definitions.
 * It is registered manually in the main {@link johnsmith.enchantingoverhauled.NeoForge} class to ensure
 * it only loads on the physical client.
 */
public class NeoForgeClient {

    public static void initialize(IEventBus eventBus) {
        eventBus.addListener(NeoForgeClient::registerLayerDefinitions);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TomeModel.LAYER_LOCATION, TomeModel::createBodyLayer);
    }
}