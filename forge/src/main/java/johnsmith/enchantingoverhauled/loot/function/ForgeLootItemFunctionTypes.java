package johnsmith.enchantingoverhauled.loot.function;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;

public final class ForgeLootItemFunctionTypes {

    public static void initialize(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.LOOT_FUNCTION_TYPE.key())) {
            for (LootItemFunctionTypes lootFunction : LootItemFunctionTypes.values()) {
                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, lootFunction.getSerializedName());
                if (!BuiltInRegistries.LOOT_FUNCTION_TYPE.containsKey(location)) {
                    event.register(
                            BuiltInRegistries.LOOT_FUNCTION_TYPE.key(),
                            location,
                            lootFunction::type
                    );
                }
            }
        }
    }

    private ForgeLootItemFunctionTypes() {}
}