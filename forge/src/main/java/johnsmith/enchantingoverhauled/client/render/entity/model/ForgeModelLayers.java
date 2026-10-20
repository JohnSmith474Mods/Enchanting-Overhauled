package johnsmith.enchantingoverhauled.client.render.entity.model;

import johnsmith.enchantingoverhauled.client.ClientConstants;

import net.minecraftforge.client.event.EntityRenderersEvent;

public final class ForgeModelLayers {
    public static void initialize(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ClientConstants.ENCHANTED_TOME_LAYER, EnchantedBookModel::createBodyLayer);
        event.registerLayerDefinition(ClientConstants.ENCHANTED_BOOK_LAYER, EnchantedBookModel::createBodyLayer);
    }

    private ForgeModelLayers() {}
}