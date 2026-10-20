package johnsmith.enchantingoverhauled.item.enchantment;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.value.LevelBasedValues;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.registries.RegisterEvent;

public final class NeoForgeLevelBasedValues {
    public static void initialize(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE.key())) {
            for (LevelBasedValues value : LevelBasedValues.values()) {
                ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, value.getSerializedName());
                if (!BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE.containsKey(loc)) {
                    Registry.register(BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, loc, value.codec());
                }
            }
        }
    }

    private NeoForgeLevelBasedValues() {}
}