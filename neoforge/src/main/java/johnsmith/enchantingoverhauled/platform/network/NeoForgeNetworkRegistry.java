package johnsmith.enchantingoverhauled.platform.network;

import johnsmith.enchantingoverhauled.client.network.ClientPayloadHandler;
import johnsmith.enchantingoverhauled.network.EnchantmentMenuSyncPayload;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NeoForgeNetworkRegistry {
    public static void initialize(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                EnchantmentMenuSyncPayload.TYPE,
                EnchantmentMenuSyncPayload.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandler.handleMenuSync(payload)
        );
    }

    private NeoForgeNetworkRegistry() {}
}