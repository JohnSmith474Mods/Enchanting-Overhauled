package johnsmith.enchantingoverhauled.event;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class EventHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void handleAnvilBreakChance(final AnvilRepairEvent event) {
        event.setBreakChance(Config.ANVIL_BREAK_CHANCE.get());
    }
}