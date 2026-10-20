package johnsmith.enchantingoverhauled.client.gui.screen;

import johnsmith.enchantingoverhauled.client.gui.screen.enchant.EnchantmentScreen;
import johnsmith.enchantingoverhauled.client.gui.screen.scribe.ScribeScreen;
import johnsmith.enchantingoverhauled.menu.MenuTypes;

import net.minecraft.client.gui.screens.MenuScreens;

public class FabricMenuScreens {
    public static void initialize() {
        MenuScreens.register(MenuTypes.ENCHANT.get(), EnchantmentScreen::new);
        MenuScreens.register(MenuTypes.SCRIBE.get(), ScribeScreen::new);
    }
}
