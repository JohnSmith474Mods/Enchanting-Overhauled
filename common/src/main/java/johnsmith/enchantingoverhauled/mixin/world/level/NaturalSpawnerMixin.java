package johnsmith.enchantingoverhauled.mixin.world.level;

import johnsmith.enchantingoverhauled.entity.spawn.StrongholdLibraryBookSpawnHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    @Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true)
    private static void enchanting_overhauled$appendStrongholdLibrarySpawns(
            ServerLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            MobCategory category,
            BlockPos pos,
            Holder<Biome> biome,
            CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir
    ) {
        WeightedRandomList<MobSpawnSettings.SpawnerData> modified =
                StrongholdLibraryBookSpawnHandler.appendIfInStrongholdLibrary(
                        level,
                        structureManager,
                        category,
                        pos,
                        cir.getReturnValue()
                );
        if (modified != cir.getReturnValue()) {
            cir.setReturnValue(modified);
        }
    }
}