package johnsmith.loot;

import com.google.common.collect.Sets;
import johnsmith.EnchantingOverhauled;
import johnsmith.item.ItemRegistry;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

import java.util.Set;

public class LootTableInjector {
    private static final Set<Identifier> RARE_LOOT_TABLES = Sets.newHashSet();
    private static final Set<Identifier> UNCOMMON_LOOT_TABLES = Sets.newHashSet();

    static {
        RARE_LOOT_TABLES.add(LootTables.ANCIENT_CITY_CHEST.getValue());
        RARE_LOOT_TABLES.add(LootTables.BASTION_TREASURE_CHEST.getValue());
        RARE_LOOT_TABLES.add(LootTables.BURIED_TREASURE_CHEST.getValue());
        RARE_LOOT_TABLES.add(LootTables.DESERT_PYRAMID_CHEST.getValue());
        RARE_LOOT_TABLES.add(LootTables.END_CITY_TREASURE_CHEST.getValue());
        RARE_LOOT_TABLES.add(LootTables.JUNGLE_TEMPLE_CHEST.getValue());
        RARE_LOOT_TABLES.add(LootTables.STRONGHOLD_LIBRARY_CHEST.getValue());
        RARE_LOOT_TABLES.add(LootTables.WOODLAND_MANSION_CHEST.getValue());
    }

    static {
        UNCOMMON_LOOT_TABLES.add(LootTables.SIMPLE_DUNGEON_CHEST.getValue());
        UNCOMMON_LOOT_TABLES.add(LootTables.DESERT_PYRAMID_ARCHAEOLOGY.getValue());
        UNCOMMON_LOOT_TABLES.add(LootTables.DESERT_WELL_ARCHAEOLOGY.getValue());
        UNCOMMON_LOOT_TABLES.add(LootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY.getValue());
        UNCOMMON_LOOT_TABLES.add(LootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY.getValue());
        UNCOMMON_LOOT_TABLES.add(LootTables.TRAIL_RUINS_RARE_ARCHAEOLOGY.getValue());
    }

    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing loot table injector...");

        LootTableEvents.MODIFY.register((resourceManager, lootManager, source) -> {
            if (RARE_LOOT_TABLES.contains(resourceManager.getValue())) {
                LootPool.Builder poolBuilder = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.05f))
                        .with(ItemEntry.builder(ItemRegistry.ENCHANTED_TOME))
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f)))
                        .apply(EnchantRandomlyLootFunction.create());
                lootManager.pool(poolBuilder);
            }
        });
        LootTableEvents.MODIFY.register((resourceManager, lootManager, source) -> {
            if (UNCOMMON_LOOT_TABLES.contains(resourceManager.getValue())) {
                LootPool.Builder poolBuilder = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.01f))
                        .with(ItemEntry.builder(ItemRegistry.ENCHANTED_TOME))
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f)))
                        .apply(EnchantRandomlyLootFunction.create());
                lootManager.pool(poolBuilder);
            }
        });
    }
}
