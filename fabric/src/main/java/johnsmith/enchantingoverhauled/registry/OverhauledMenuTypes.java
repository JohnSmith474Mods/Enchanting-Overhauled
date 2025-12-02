package johnsmith.enchantingoverhauled.registry;

import johnsmith.enchantingoverhauled.Common;
import johnsmith.enchantingoverhauled.enchantment.OverhauledEnchantmentMenu;
import johnsmith.enchantingoverhauled.enchantment.OverhauledEnchantmentScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;

public class OverhauledMenuTypes {
    public static MenuType<OverhauledEnchantmentMenu> OVERHAULED_ENCHANTMENT_MENU;

    public static void initialize() {
        OVERHAULED_ENCHANTMENT_MENU = register("overhauled_enchanting_menu", new MenuType<>(
                (containerId, playerInventory) -> new OverhauledEnchantmentMenu(containerId, playerInventory, ContainerLevelAccess.NULL), FeatureFlags.DEFAULT_FLAGS));

        MenuScreens.register(OVERHAULED_ENCHANTMENT_MENU, OverhauledEnchantmentScreen::new);
    }

    private static <T extends AbstractContainerMenu> MenuType<T> register(final String path, final MenuType<T> type) {
        return Registry.register(BuiltInRegistries.MENU, Common.resource(path), type);
    }
}
