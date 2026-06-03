package johnsmith.enchantingoverhauled.client.gui.screen;

import static johnsmith.enchantingoverhauled.menu.MenuTypes.ENCHANTMENT_MENU;

public class MenuScreens {
    public static void initialize() {
        net.minecraft.client.gui.screens.MenuScreens.register(ENCHANTMENT_MENU, EnchantmentScreen::new);
    }
}