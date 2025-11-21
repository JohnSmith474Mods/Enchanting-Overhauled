package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.client.render.entity.model.TomeModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-side event handler for the NeoForge platform.
 * <p>
 * This class encapsulates client-specific initialization logic, such as registering model layer definitions.
 * It is registered manually in the main {@link johnsmith.enchantingoverhauled.NeoForge} class to ensure
 * it only loads on the physical client.
 */
public class NeoForgeClient {

    /**
     * Initializes the client-side event listeners.
     *
     * @param eventBus The mod-specific event bus to listen to.
     */
    public static void initialize(IEventBus eventBus) {
        eventBus.addListener(NeoForgeClient::registerLayerDefinitions);
    }

    /**
     * Event listener for registering entity model layer definitions.
     * <p>
     * This method registers the {@link TomeModel} geometry with the game's entity renderer system.
     *
     * @param event The register layer definitions event.
     */
    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TomeModel.LAYER_LOCATION, TomeModel::createBodyLayer);
    }
}