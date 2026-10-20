package johnsmith.enchantingoverhauled.platform.network;

import johnsmith.enchantingoverhauled.network.EnchantmentMenuSyncPayload;
import johnsmith.enchantingoverhauled.platform.services.NetworkHelper;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;

public class FabricNetworkHelper implements NetworkHelper {

    @Override
    public void sendToClient(EnchantmentMenuSyncPayload payload, ServerPlayer serverPlayer) {
        ServerPlayNetworking.send(serverPlayer, payload);
    }
}
