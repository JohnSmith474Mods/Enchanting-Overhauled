package johnsmith.enchantingoverhauled.loot.condition;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;

public final class ForgeLootItemConditionTypes {

    public static void initialize(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.LOOT_CONDITION_TYPE.key())) {
            for (LootItemConditionTypes conditionType : LootItemConditionTypes.values()) {
                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, conditionType.getSerializedName());
                if (!BuiltInRegistries.LOOT_CONDITION_TYPE.containsKey(location)) {
                    event.register(
                            BuiltInRegistries.LOOT_CONDITION_TYPE.key(),
                            location,
                            conditionType::type
                    );
                }
            }
        }
    }

    private ForgeLootItemConditionTypes() {}
}