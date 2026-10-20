package johnsmith.enchantingoverhauled.loot.function;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class NeoForgeLootItemFunctionTypes {
    public static void initialize(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.LOOT_FUNCTION_TYPE.key())) {
            for (LootItemFunctionTypes lootFunction : LootItemFunctionTypes.values()) {
                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, lootFunction.getSerializedName());
                if (!BuiltInRegistries.LOOT_FUNCTION_TYPE.containsKey(location)) {
                    Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, location, lootFunction.type());
                }
            }
        }
    }

    private NeoForgeLootItemFunctionTypes() {}
}