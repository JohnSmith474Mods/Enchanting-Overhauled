package johnsmith.enchantingoverhauled.datagen;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.loot.TomeLootModifier;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NeoForgeLootModifiersProvider extends GlobalLootModifierProvider {

    public NeoForgeLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Constants.MOD_ID);
    }

    @Override
    protected void start() {
        // 1. Define the "Rare" tables (10% chance)
        List<String> rareTables = List.of(
                "minecraft:chests/ancient_city",
                "minecraft:chests/bastion_treasure",
                "minecraft:chests/buried_treasure",
                "minecraft:chests/end_city_treasure",
                "minecraft:chests/jungle_temple",
                "minecraft:chests/stronghold_library",
                "minecraft:chests/woodland_mansion",
                "minecraft:archaeology/trail_ruins_rare"
        );

        // 2. Define the "Uncommon" tables (5% chance)
        List<String> uncommonTables = List.of(
                "minecraft:chests/simple_dungeon",
                "minecraft:chests/desert_pyramid",
                "minecraft:archaeology/desert_pyramid",
                "minecraft:archaeology/desert_well",
                "minecraft:archaeology/ocean_ruin_warm",
                "minecraft:archaeology/ocean_ruin_cold"
        );

        // 3. Register the Rare Modifier
        this.add("inject_tome_rare", new TomeLootModifier(
                new LootItemCondition[]{
                        createTableFilter(rareTables)
                },
                "rare" // Passes "rare" to your modifier's constructor
        ));

        // 4. Register the Uncommon Modifier
        this.add("inject_tome_uncommon", new TomeLootModifier(
                new LootItemCondition[]{
                        createTableFilter(uncommonTables)
                },
                "uncommon" // Passes "uncommon" to your modifier's constructor
        ));
    }

    /**
     * Helper to create a single condition that matches ANY of the provided table IDs.
     * Wraps multiple LootTableIdCondition inside an AnyOfCondition.
     */
    private LootItemCondition createTableFilter(List<String> tables) {
        AnyOfCondition.Builder builder = AnyOfCondition.anyOf();

        for (String table : tables) {
            builder.or(LootTableIdCondition.builder(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, table)));
        }

        return builder.build();
    }
}