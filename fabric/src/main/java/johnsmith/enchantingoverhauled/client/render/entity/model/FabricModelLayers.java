package johnsmith.enchantingoverhauled.client.render.entity.model;

import johnsmith.enchantingoverhauled.client.ClientConstants;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public final class FabricModelLayers {
    public static void initialize() {
        EntityModelLayerRegistry.registerModelLayer(ClientConstants.ENCHANTED_TOME_LAYER, EnchantedBookModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ClientConstants.ENCHANTED_BOOK_LAYER, EnchantedBookModel::createBodyLayer);
    }

    private FabricModelLayers() {}
}
