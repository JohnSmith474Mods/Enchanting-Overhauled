package johnsmith.enchantingoverhauled.loot;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

public final class ForgeLootInjector {

    public static void initialize() {
        MinecraftForge.EVENT_BUS.addListener(ForgeLootInjector::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(ForgeLootInjector::onDatapackSync);
    }

    private static void onServerStarted(ServerStartedEvent event) {
        LootInjectionHandler.applyInjections(event.getServer());
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            LootInjectionHandler.applyInjections(event.getPlayerList().getServer());
        }
    }

    private ForgeLootInjector() {}
}