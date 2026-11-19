package johnsmith.enchantingoverhauled.server;

import johnsmith.enchantingoverhauled.api.enchantment.value.DifficultyContext;

import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

public final class ForgeServer {
    public static void initialize() {
        ServerStartingEvent.BUS.addListener(
                (ServerStartingEvent event) -> DifficultyContext.setServer(event.getServer())
        );
        ServerStoppedEvent.BUS.addListener(
                (ServerStoppedEvent event) -> DifficultyContext.setServer(null)
        );
    }

    private ForgeServer() {}
}
