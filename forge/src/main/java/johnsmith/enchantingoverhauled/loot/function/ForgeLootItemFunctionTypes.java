package johnsmith.enchantingoverhauled.loot.function;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraftforge.registries.RegisterEvent;

public final class ForgeLootItemFunctionTypes {

    public static void initialize(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.LOOT_FUNCTION_TYPE.key())) {
            for (LootItemFunctionTypes lootFunction : LootItemFunctionTypes.values()) {
                Identifier location = Identifier.fromNamespaceAndPath(Constants.MOD_ID, lootFunction.getSerializedName());
                if (!BuiltInRegistries.LOOT_FUNCTION_TYPE.containsKey(location)) {
                    event.register(
                            BuiltInRegistries.LOOT_FUNCTION_TYPE.key(),
                            location,
                            lootFunction::codec
                    );
                }
            }
        }
    }

    private ForgeLootItemFunctionTypes() {}
}