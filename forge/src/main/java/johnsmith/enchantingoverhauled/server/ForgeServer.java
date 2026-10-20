package johnsmith.enchantingoverhauled.server;

import johnsmith.enchantingoverhauled.api.enchantment.value.DifficultyContext;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

public final class ForgeServer {
    public static void initialize() {
        MinecraftForge.EVENT_BUS.addListener(
                (ServerStartingEvent event) -> DifficultyContext.setServer(event.getServer())
        );
        MinecraftForge.EVENT_BUS.addListener(
                (ServerStoppedEvent event) -> DifficultyContext.setServer(null)
        );
    }

    private ForgeServer() {}
}
