package johnsmith.enchantingoverhauled.events;

import johnsmith.enchantingoverhauled.config.Config;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;

@EventBusSubscriber
public class EventHandler {
    @SubscribeEvent(priority = EventPriority.HIGH) // Adjust the base chance a bit earlier
    public static void handleAnvilBreakChance(final AnvilRepairEvent event) {
        event.setBreakChance(Config.BOUNDED_ANVIL_BREAK_CHANCE.get());
    }
}
