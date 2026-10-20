package johnsmith.enchantingoverhauled.entity;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.tag.Tags;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class FabricEntities {

    public static void initialize() {
        for (Entities entity : Entities.values()) {
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, entity.getSerializedName()),
                    entity.build()
            );
        }

        SpawnPlacements.register(
                Entities.ENCHANTED_BOOK.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EnchantedBookEntity::checkEnchantedBookSpawnRules
        );

        BiomeModifications.addSpawn(
                BiomeSelectors.tag(Tags.SPAWNS_ENCHANTED_BOOK),
                MobCategory.MONSTER,
                Entities.ENCHANTED_BOOK.get(),
                EnchantedBookEntity.NATURAL_SPAWN_WEIGHT,
                EnchantedBookEntity.NATURAL_SPAWN_MIN_GROUP,
                EnchantedBookEntity.NATURAL_SPAWN_MAX_GROUP
        );
    }
}