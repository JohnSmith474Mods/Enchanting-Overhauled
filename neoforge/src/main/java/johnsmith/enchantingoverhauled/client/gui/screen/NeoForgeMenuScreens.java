package johnsmith.enchantingoverhauled.client.gui.screen;

import johnsmith.enchantingoverhauled.client.gui.screen.enchant.EnchantmentScreen;
import johnsmith.enchantingoverhauled.client.gui.screen.scribe.ScribeScreen;
import johnsmith.enchantingoverhauled.menu.MenuTypes;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class NeoForgeMenuScreens {
    public static void initialize(RegisterMenuScreensEvent event) {
        event.register(MenuTypes.ENCHANT.get(), EnchantmentScreen::new);
        event.register(MenuTypes.SCRIBE.get(), ScribeScreen::new);
    }

    private NeoForgeMenuScreens() {}
}