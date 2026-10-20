package johnsmith.enchantingoverhauled.server;

import johnsmith.enchantingoverhauled.api.enchantment.value.DifficultyContext;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class FabricServer {
    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(DifficultyContext::setServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> DifficultyContext.setServer(null));
    }

    private FabricServer() {}
}
