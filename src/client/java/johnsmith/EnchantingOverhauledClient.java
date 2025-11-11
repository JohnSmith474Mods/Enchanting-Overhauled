package johnsmith;

import johnsmith.client.render.entity.model.TomeModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class EnchantingOverhauledClient implements ClientModInitializer {

    public static final EntityModelLayer CUSTOM_BOOK_LAYER =
            new EntityModelLayer(new Identifier(EnchantingOverhauled.MOD_ID, "enchanting_book"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(CUSTOM_BOOK_LAYER, TomeModel::getTexturedModelData);
    }
}