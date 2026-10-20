package johnsmith.enchantingoverhauled.item.enchantment;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.value.LevelBasedValues;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class FabricLevelBasedValues {
    public static void initialize() {
        for (LevelBasedValues value : LevelBasedValues.values()) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, value.getSerializedName());
            if (!BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE.containsKey(loc)) {
                Registry.register(BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, loc, value.codec());
            }
        }
    }

    private FabricLevelBasedValues() {}
}
