package johnsmith.enchantingoverhauled.entity.spawn;

import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.entity.Entities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class StrongholdLibraryBookSpawnHandler {

    private static final Weighted<MobSpawnSettings.SpawnerData> TOME_SPAWNER = new Weighted<>(
            new MobSpawnSettings.SpawnerData(
                    Entities.ENCHANTED_TOME.get(),
                    ConstantInt.of(1)
            ),
            2
    );

    private static final Weighted<MobSpawnSettings.SpawnerData> BOOK_SPAWNER = new Weighted<>(
            new MobSpawnSettings.SpawnerData(
                    Entities.ENCHANTED_BOOK.get(),
                    UniformInt.of(1, 3)
            ),
            12
    );

    private StrongholdLibraryBookSpawnHandler() {}

    public static WeightedList<MobSpawnSettings.SpawnerData> appendIfInStrongholdLibrary(
            ServerLevel level,
            StructureManager structureManager,
            MobCategory category,
            BlockPos pos,
            WeightedList<MobSpawnSettings.SpawnerData> original
    ) {
        if (category != MobCategory.MONSTER) {
            return original;
        }
        if (!isInsideStrongholdLibrary(level, structureManager, pos)) {
            return original;
        }

        List<Weighted<MobSpawnSettings.SpawnerData>> combined = new ArrayList<>(original.unwrap());
        combined.add(TOME_SPAWNER);
        combined.add(BOOK_SPAWNER);
        return WeightedList.of(combined);
    }

    public static boolean isInsideStrongholdLibrary(
            ServerLevel level,
            StructureManager structureManager,
            BlockPos pos
    ) {
        var registryOpt = level.registryAccess().lookup(Registries.STRUCTURE);
        if (registryOpt.isEmpty()) {
            return false;
        }
        Optional<Holder.Reference<Structure>> strongholdHolder = registryOpt.get().get(BuiltinStructures.STRONGHOLD);
        if (strongholdHolder.isEmpty()) {
            return false;
        }

        StructureStart start = structureManager.getStructureAt(pos, strongholdHolder.get().value());
        if (!start.isValid()) {
            return false;
        }

        for (StructurePiece piece : start.getPieces()) {
            if (piece.getBoundingBox().isInside(pos)) {
                if (piece instanceof StrongholdPieces.Library) {
                    return true;
                }
            }
        }

        for (BlockPos checkPos : BlockPos.withinBoxByManhattanDistance(pos, 8, 5, 8)) {
            if (level.getBlockState(checkPos).is(Tags.ENCHANTED_BOOK_IDLE_TARGET)) {
                return true;
            }
        }
        return false;
    }
}