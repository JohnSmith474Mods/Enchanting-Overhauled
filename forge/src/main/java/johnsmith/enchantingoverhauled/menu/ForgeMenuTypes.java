package johnsmith.enchantingoverhauled.menu;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.menu.enchant.EnchantmentMenu;
import johnsmith.enchantingoverhauled.menu.scribe.ScribeMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public final class ForgeMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Constants.MOD_ID);

    public static void initialize(IEventBus eventBus) {
        MENU_TYPES.register(
                MenuTypes.ENCHANT.getSerializedName(),
                () -> MenuTypes.ENCHANT.build(new MenuType<>(EnchantmentMenu::new, FeatureFlags.DEFAULT_FLAGS))
        );
        MENU_TYPES.register(
                MenuTypes.SCRIBE.getSerializedName(),
                () -> MenuTypes.SCRIBE.build(new MenuType<>(ScribeMenu::new, FeatureFlags.DEFAULT_FLAGS))
        );
        MENU_TYPES.register(eventBus);
    }

    private ForgeMenuTypes() {}
}