package johnsmith.enchantingoverhauled.menu;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.client.gui.screen.EnchantmentScreen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class MenuTypes {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Constants.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<EnchantmentMenu>> ENCHANTMENT_MENU = MENU_TYPES.register(
        "enchantment_menu", () -> new MenuType<>((containerId, playerInventory) -> new EnchantmentMenu(containerId, playerInventory, ContainerLevelAccess.NULL), FeatureFlags.DEFAULT_FLAGS)
    );

    public static void initialize(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void registerMenu(final RegisterMenuScreensEvent event) {
        event.register(ENCHANTMENT_MENU.value(), EnchantmentScreen::new);
    }
}
