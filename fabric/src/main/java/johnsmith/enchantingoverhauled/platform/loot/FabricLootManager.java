package johnsmith.enchantingoverhauled.platform.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.mixin.accessor.LootTableAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FabricLootManager {
    private static final Gson GSON = new Gson();
    private static final Map<ResourceLocation, String> INJECTION_MAP = new HashMap<>();

    public static void reload(ResourceManager resourceManager) {
        INJECTION_MAP.clear();
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources("loot_injections", path -> path.getPath().endsWith(".json")).entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                for (Map.Entry<String, JsonElement> jsonEntry : json.entrySet()) {
                    // Update: Use ResourceLocation.parse
                    INJECTION_MAP.put(ResourceLocation.parse(jsonEntry.getKey()), jsonEntry.getValue().getAsString());
                }
            } catch (Exception e) {
                Constants.LOG.error("Failed to load loot injections: " + entry.getKey(), e);
            }
        }
        Constants.LOG.info("Loaded {} loot injection targets.", INJECTION_MAP.size());
    }

    /**
     * Attempts to inject the Enchanted Tome loot pool.
     * <p>
     * Update: Now requires {@link HolderLookup.Provider} to support the new Enchantment registry lookups.
     */
    public static void tryInject(LootTable table, ResourceLocation tableId, HolderLookup.Provider registries) {
        if (INJECTION_MAP.containsKey(tableId)) {
            String rarity = INJECTION_MAP.get(tableId);
            float chance = getChanceForRarity(rarity);
            if (chance > 0) {
                injectPool(table, chance, registries);
            }
        }
    }

    private static void injectPool(LootTable table, float chance, HolderLookup.Provider registries) {
        LootPool pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_tome")))
                        // Update: Pass registries to the builder method
                        .apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries))
                )
                .build();

        List<LootPool> currentPools = new ArrayList<>(((LootTableAccessor) table).enchanting_overhauled$getPools());
        currentPools.add(pool);
        ((LootTableAccessor) table).enchanting_overhauled$setPools(currentPools);
    }

    private static float getChanceForRarity(String rarity) {
        // Update: Call .get() on Config objects
        return switch (rarity.toUpperCase()) {
            case "EPIC" -> Config.BOUNDED_LOOT_CHANCE_EPIC.get();
            case "RARE" -> Config.BOUNDED_LOOT_CHANCE_RARE.get();
            case "UNCOMMON" -> Config.BOUNDED_LOOT_CHANCE_UNCOMMON.get();
            case "COMMON" -> Config.BOUNDED_LOOT_CHANCE_COMMON.get();
            default -> {
                Constants.LOG.warn("Unknown loot injection rarity: {}", rarity);
                yield 0.0f;
            }
        };
    }
}