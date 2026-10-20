package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.client.gui.screen.FabricMenuScreens;
import johnsmith.enchantingoverhauled.client.particle.FabricSimpleParticleTypes;
import johnsmith.enchantingoverhauled.client.render.entity.FabricEntityRenderers;
import johnsmith.enchantingoverhauled.client.render.entity.model.FabricModelLayers;
import johnsmith.enchantingoverhauled.platform.network.FabricNetworkRegistry;

import net.fabricmc.api.ClientModInitializer;

public class FabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Common init
        Common.initialize();

        // Platform init
        FabricMenuScreens.initialize();
        FabricSimpleParticleTypes.initialize();
        FabricModelLayers.initialize();
        FabricEntityRenderers.initialize();
        FabricNetworkRegistry.initializeClient();
    }
}