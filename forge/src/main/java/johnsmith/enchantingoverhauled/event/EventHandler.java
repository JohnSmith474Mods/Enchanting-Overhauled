package johnsmith.enchantingoverhauled.event;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent(priority = Priority.HIGH)
    public static void handleAnvilBreakChance(final AnvilRepairEvent event) {
        event.setBreakChance(Config.ANVIL_BREAK_CHANCE.get());
    }
}