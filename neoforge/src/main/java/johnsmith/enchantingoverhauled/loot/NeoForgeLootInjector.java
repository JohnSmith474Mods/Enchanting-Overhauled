package johnsmith.enchantingoverhauled.loot;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public final class NeoForgeLootInjector {
    public static void initialize() {
        NeoForge.EVENT_BUS.addListener(NeoForgeLootInjector::onServerStarted);
        NeoForge.EVENT_BUS.addListener(NeoForgeLootInjector::onDatapackSync);
    }

    private static void onServerStarted(ServerStartedEvent event) {
        LootInjectionHandler.applyInjections(event.getServer());
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            LootInjectionHandler.applyInjections(event.getPlayerList().getServer());
        }
    }

    private NeoForgeLootInjector() {}
}