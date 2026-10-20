package johnsmith.enchantingoverhauled.loot.function;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class FabricLootItemFunctionTypes {
    public static void initialize() {
        for (LootItemFunctionTypes lootFunction : LootItemFunctionTypes.values()) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, lootFunction.getSerializedName());
            if (!BuiltInRegistries.LOOT_FUNCTION_TYPE.containsKey(location)) {
                Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, location, lootFunction.type());
            }
        }
    }

    private FabricLootItemFunctionTypes() {}
}
