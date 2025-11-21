package johnsmith.enchantingoverhauled.platform.loot;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.mixin.accessor.LootTableAccessor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages loot table injections for the Fabric platform.
 * <p>
 * This manager loads data-driven injection rules from data packs (located in {@code loot_injections/})
 * and modifies vanilla {@link LootTable} objects at runtime to include the Enchanted Tome.
 * unlike NeoForge's Global Loot Modifiers, this system directly mutates the loot table's pool list via Mixin.
 */
public class FabricLootManager {
    private static final Gson GSON = new Gson();
    /** Maps LootTable IDs to their assigned rarity string (e.g., "minecraft:chests/simple_dungeon" -> "uncommon"). */
    private static final Map<ResourceLocation, String> INJECTION_MAP = new HashMap<>();

    /**
     * Reloads loot injection data from the provided resource manager.
     * <p>
     * Scans for JSON files in the {@code loot_injections} directory of all data packs.
     * Parses these files to populate the internal injection map, which dictates which
     * loot tables receive the Enchanted Tome and at what rarity.
     *
     * @param resourceManager The server's resource manager.
     */
    public static void reload(ResourceManager resourceManager) {
        INJECTION_MAP.clear();
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources("loot_injections", path -> path.getPath().endsWith(".json")).entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                for (Map.Entry<String, JsonElement> jsonEntry : json.entrySet()) {
                    INJECTION_MAP.put(new ResourceLocation(jsonEntry.getKey()), jsonEntry.getValue().getAsString());
                }
            } catch (Exception e) {
                Constants.LOG.error("Failed to load loot injections: " + entry.getKey(), e);
            }
        }
        Constants.LOG.info("Loaded {} loot injection targets.", INJECTION_MAP.size());
    }

    /**
     * Attempts to inject the Enchanted Tome loot pool into the specified loot table.
     * <p>
     * Checks if the provided table ID exists in the loaded injection map. If a match is found,
     * a new loot pool containing the Enchanted Tome is constructed based on the mapped rarity
     * and added to the table.
     *
     * @param table   The loot table instance to modify.
     * @param tableId The unique registry ID of the loot table.
     */
    public static void tryInject(LootTable table, ResourceLocation tableId) {
        if (INJECTION_MAP.containsKey(tableId)) {
            String rarity = INJECTION_MAP.get(tableId);
            float chance = getChanceForRarity(rarity);
            if (chance > 0) {
                injectPool(table, chance);
            }
        }
    }

    /**
     * Constructs and injects a new loot pool into the given table using the accessor mixin.
     * <p>
     * The injected pool has a single roll and a random chance condition defined by the {@code chance} parameter.
     * It drops a single {@code enchanting_overhauled:enchanted_tome} with a random enchantment applied.
     *
     * @param table  The loot table to modify.
     * @param chance The probability (0.0 - 1.0) that the pool will generate loot.
     */
    private static void injectPool(LootTable table, float chance) {
        LootPool pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(new ResourceLocation(Constants.MOD_ID, "enchanted_tome")))
                        .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                )
                .build();

        // Use Mixin accessor to get the mutable list of pools, add the new one, and set it back.
        List<LootPool> currentPools = new ArrayList<>(((LootTableAccessor) table).enchanting_overhauled$getPools());
        currentPools.add(pool);
        ((LootTableAccessor) table).enchanting_overhauled$setPools(currentPools);
    }

    /**
     * Resolves the loot chance configuration value based on the provided rarity string.
     *
     * @param rarity The rarity key (e.g., "rare", "uncommon").
     * @return The chance float from the global configuration, or 0.0f if the rarity is unknown.
     */
    private static float getChanceForRarity(String rarity) {
        return switch (rarity.toUpperCase()) {
            case "EPIC" -> Config.EPIC_LOOT_CHANCE;
            case "RARE" -> Config.RARE_LOOT_CHANCE;
            case "UNCOMMON" -> Config.UNCOMMON_LOOT_CHANCE;
            case "COMMON" -> Config.COMMON_LOOT_CHANCE;
            default -> {
                Constants.LOG.warn("Unknown loot injection rarity: {}", rarity);
                yield 0.0f;
            }
        };
    }
}