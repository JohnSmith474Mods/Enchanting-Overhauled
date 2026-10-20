package johnsmith.enchantingoverhauled.entity.spawn;

import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.entity.Entities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
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

    private static final MobSpawnSettings.SpawnerData TOME_SPAWNER = new MobSpawnSettings.SpawnerData(
            Entities.ENCHANTED_TOME.get(),
            2,
            1,
            1
    );

    private static final MobSpawnSettings.SpawnerData BOOK_SPAWNER = new MobSpawnSettings.SpawnerData(
            Entities.ENCHANTED_BOOK.get(),
            12,
            1,
            3
    );

    private StrongholdLibraryBookSpawnHandler() {}

    public static WeightedRandomList<MobSpawnSettings.SpawnerData> appendIfInStrongholdLibrary(
            ServerLevel level,
            StructureManager structureManager,
            MobCategory category,
            BlockPos pos,
            WeightedRandomList<MobSpawnSettings.SpawnerData> original
    ) {
        if (category != MobCategory.MONSTER) {
            return original;
        }
        if (!isInsideStrongholdLibrary(level, structureManager, pos)) {
            return original;
        }

        List<MobSpawnSettings.SpawnerData> combined = new ArrayList<>(original.unwrap());
        combined.add(TOME_SPAWNER);
        combined.add(BOOK_SPAWNER);
        return WeightedRandomList.create(combined);
    }

    public static boolean isInsideStrongholdLibrary(
            ServerLevel level,
            StructureManager structureManager,
            BlockPos pos
    ) {
        var registryOpt = level.registryAccess().registry(Registries.STRUCTURE);
        if (registryOpt.isEmpty()) {
            return false;
        }
        Optional<Holder.Reference<Structure>> strongholdHolder = registryOpt.get().getHolder(BuiltinStructures.STRONGHOLD);
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

        return BlockPos.findClosestMatch(
                pos,
                8,
                5,
                checkPos -> level.getBlockState(checkPos).is(Tags.ENCHANTED_BOOK_IDLE_TARGET)
        ).isPresent();
    }
}