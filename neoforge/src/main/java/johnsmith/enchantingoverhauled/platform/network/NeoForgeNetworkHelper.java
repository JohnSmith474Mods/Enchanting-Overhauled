package johnsmith.enchantingoverhauled.platform.network;

import johnsmith.enchantingoverhauled.network.EnchantmentMenuSyncPayload;
import johnsmith.enchantingoverhauled.platform.services.NetworkHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeNetworkHelper implements NetworkHelper {

    @Override
    public void sendToClient(EnchantmentMenuSyncPayload payload, ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, payload);
    }
}