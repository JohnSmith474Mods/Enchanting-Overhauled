package johnsmith.enchantingoverhauled.loot;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FabricLootInjector {
    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(LootInjectionHandler::applyInjections);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                LootInjectionHandler.applyInjections(server);
            }
        });
    }
}