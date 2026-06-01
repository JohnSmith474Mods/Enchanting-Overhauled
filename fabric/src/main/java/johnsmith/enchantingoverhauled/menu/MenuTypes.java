package johnsmith.enchantingoverhauled.menu;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;


public class MenuTypes {
    public static MenuType<EnchantmentMenu> ENCHANTMENT_MENU = register(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchantment_menu"),
            new MenuType<>((containerId, playerInventory) -> new EnchantmentMenu(containerId, playerInventory, ContainerLevelAccess.NULL), FeatureFlags.DEFAULT_FLAGS)
    );

    private static <T extends AbstractContainerMenu> MenuType<T> register(final ResourceLocation location, final MenuType<T> type) {
        return Registry.register(BuiltInRegistries.MENU, location, type);
    }

    public static void initialize() {}
}
