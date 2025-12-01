package johnsmith.enchantingoverhauled.registry;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.enchantment.OverhauledEnchantmentMenu;
import johnsmith.enchantingoverhauled.enchantment.OverhauledEnchantmentScreen;
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
public class OverhauledMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Constants.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<OverhauledEnchantmentMenu>> OVERHAULED_ENCHANTMENT_MENU = MENU_TYPES.register("overhauled_enchanting_menu", () -> new MenuType<>(
            (containerId, playerInventory) -> new OverhauledEnchantmentMenu(containerId, playerInventory, ContainerLevelAccess.NULL), FeatureFlags.DEFAULT_FLAGS)
    );

    public static void register(final IEventBus bus) {
        MENU_TYPES.register(bus);
    }

    @SubscribeEvent
    public static void registerMenu(final RegisterMenuScreensEvent event) {
        event.register(OVERHAULED_ENCHANTMENT_MENU.value(), OverhauledEnchantmentScreen::new);
    }
}
