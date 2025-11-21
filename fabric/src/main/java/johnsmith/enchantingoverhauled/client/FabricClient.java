package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.client.render.entity.model.TomeModel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

/**
 * Client-side initializer for the Fabric platform.
 * <p>
 * This class is responsible for registering client-only resources such as entity model layers.
 * It implements {@link ClientModInitializer} to run during the client startup phase.
 */
public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(TomeModel.LAYER_LOCATION, TomeModel::createBodyLayer);
    }
}