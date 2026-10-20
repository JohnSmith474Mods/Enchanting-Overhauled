package johnsmith.enchantingoverhauled.platform.network;

import johnsmith.enchantingoverhauled.network.EnchantmentMenuSyncPayload;
import johnsmith.enchantingoverhauled.platform.services.NetworkHelper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class ForgeNetworkHelper implements NetworkHelper {

    @Override
    public void sendToClient(EnchantmentMenuSyncPayload payload, ServerPlayer serverPlayer) {
        ForgeNetworkRegistry.CHANNEL.send(payload, PacketDistributor.PLAYER.with(serverPlayer));
    }
}