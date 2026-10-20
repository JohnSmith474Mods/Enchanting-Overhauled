package johnsmith.enchantingoverhauled.loot.condition;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class FabricLootItemConditionTypes {
    public static void initialize() {
        for (LootItemConditionTypes conditionType : LootItemConditionTypes.values()) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, conditionType.getSerializedName());
            if (!BuiltInRegistries.LOOT_CONDITION_TYPE.containsKey(location)) {
                Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, location, conditionType.type());
            }
        }
    }

    private FabricLootItemConditionTypes() {}
}
