package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.client.render.entity.model.TomeModel;
import johnsmith.enchantingoverhauled.config.Config;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.loader.api.FabricLoader;

public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(TomeModel.LAYER_LOCATION, TomeModel::createBodyLayer);
    }
}