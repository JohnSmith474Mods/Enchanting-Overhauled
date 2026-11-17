package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.client.gui.screen.MenuScreens;
import johnsmith.enchantingoverhauled.client.render.entity.model.TomeModel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.initialize();

        EntityModelLayerRegistry.registerModelLayer(TomeModel.LAYER_LOCATION, TomeModel::createBodyLayer);
    }
}