package johnsmith.enchantingoverhauled.client.gui.screen;

import johnsmith.enchantingoverhauled.client.gui.screen.enchant.EnchantmentScreen;
import johnsmith.enchantingoverhauled.client.gui.screen.scribe.ScribeScreen;
import johnsmith.enchantingoverhauled.menu.MenuTypes;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ForgeMenuScreens {
    public static void initialize(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MenuTypes.ENCHANT.get(), EnchantmentScreen::new);
            MenuScreens.register(MenuTypes.SCRIBE.get(), ScribeScreen::new);
        });
    }

    private ForgeMenuScreens() {}
}