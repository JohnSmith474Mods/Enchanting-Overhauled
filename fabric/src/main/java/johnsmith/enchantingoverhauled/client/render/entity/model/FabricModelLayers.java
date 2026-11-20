package johnsmith.enchantingoverhauled.client.render.entity.model;

import johnsmith.enchantingoverhauled.client.ClientConstants;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

public final class FabricModelLayers {
    public static void initialize() {
        ModelLayerRegistry.registerModelLayer(ClientConstants.ENCHANTED_TOME_LAYER, EnchantedBookModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientConstants.ENCHANTED_BOOK_LAYER, EnchantedBookModel::createBodyLayer);
    }

    private FabricModelLayers() {}
}
