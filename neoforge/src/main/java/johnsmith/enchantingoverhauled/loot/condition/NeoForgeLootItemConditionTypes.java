package johnsmith.enchantingoverhauled.loot.condition;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class NeoForgeLootItemConditionTypes {
    public static void initialize(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.LOOT_CONDITION_TYPE.key())) {
            for (LootItemConditionTypes conditionType : LootItemConditionTypes.values()) {
                Identifier location = Identifier.fromNamespaceAndPath(Constants.MOD_ID, conditionType.getSerializedName());
                if (!BuiltInRegistries.LOOT_CONDITION_TYPE.containsKey(location)) {
                    Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, location, conditionType.type());
                }
            }
        }
    }

    private NeoForgeLootItemConditionTypes() {}
}