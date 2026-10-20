package johnsmith.enchantingoverhauled.server;

import johnsmith.enchantingoverhauled.api.enchantment.value.DifficultyContext;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

public final class NeoForgeServer {
    public static void initailize() {
        NeoForge.EVENT_BUS.addListener(
                (ServerStartingEvent event) -> DifficultyContext.setServer(event.getServer())
        );
        NeoForge.EVENT_BUS.addListener(
                (ServerStoppedEvent event) -> DifficultyContext.setServer(null)
        );
    }

    private NeoForgeServer() {}
}
