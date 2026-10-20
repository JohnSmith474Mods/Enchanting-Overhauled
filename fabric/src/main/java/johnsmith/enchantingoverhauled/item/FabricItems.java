package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class FabricItems {
    public static void initialize() {
        for (Items item : Items.values()) {
            Registry.register(
                    BuiltInRegistries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, item.getSerializedName()),
                    item.build()
            );
        }
    }
}