package johnsmith.enchantingoverhauled.platform.network;

import johnsmith.enchantingoverhauled.client.network.ClientPayloadHandler;
import johnsmith.enchantingoverhauled.network.EnchantmentMenuSyncPayload;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class FabricNetworkRegistry {
    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(EnchantmentMenuSyncPayload.TYPE, EnchantmentMenuSyncPayload.STREAM_CODEC);
    }

    @Environment(EnvType.CLIENT)
    public static void initializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(EnchantmentMenuSyncPayload.TYPE, (payload, context) -> {
            ClientPayloadHandler.handleMenuSync(payload);
        });
    }
}