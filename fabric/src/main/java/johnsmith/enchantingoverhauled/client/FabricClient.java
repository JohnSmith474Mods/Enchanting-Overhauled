package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.client.model.property.BowPullProperty;
import johnsmith.enchantingoverhauled.client.render.entity.model.TomeModel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(TomeModel.LAYER_LOCATION, TomeModel::createBodyLayer);
    }
}