package johnsmith.enchantingoverhauled.menu;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.menu.enchant.EnchantmentMenu;
import johnsmith.enchantingoverhauled.menu.scribe.ScribeMenu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class FabricMenuTypes {
    public static void initialize() {
        Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, MenuTypes.ENCHANT.getSerializedName()),
                MenuTypes.ENCHANT.build(new MenuType<>(
                        EnchantmentMenu::new,
                        FeatureFlags.DEFAULT_FLAGS
                ))
        );

        Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, MenuTypes.SCRIBE.getSerializedName()),
                MenuTypes.SCRIBE.build(new MenuType<>(
                        ScribeMenu::new,
                        FeatureFlags.DEFAULT_FLAGS
                ))
        );
    }
}