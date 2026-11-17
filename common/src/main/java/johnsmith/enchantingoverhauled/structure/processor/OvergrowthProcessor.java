package johnsmith.enchantingoverhauled.structure.processor;

import johnsmith.enchantingoverhauled.platform.Services;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/**
 * A structure processor that adds foliage (Jungle Leaves) to structures.
 * <p>
 * Takes an air block in the structure template and checks the block directly below it.
 * If the block below is wood, a bookshelf, or obsidian, it has a chance to spawn
 * persistent Jungle Leaves at the current position.
 */
public class OvergrowthProcessor extends StructureProcessor {

    public static final MapCodec<OvergrowthProcessor> CODEC = Codec.FLOAT
            .fieldOf("probability")
            .xmap(OvergrowthProcessor::new, overgrowthProcessor -> overgrowthProcessor.probability);

    private final float probability;

    /**
     * Constructs a new OvergrowthProcessor.
     *
     * @param probability The chance (0.0 - 1.0) to spawn leaves above a valid block.
     */
    public OvergrowthProcessor(float probability) {
        this.probability = probability;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            @NotNull LevelReader world,
            @NotNull BlockPos pos,
            @NotNull BlockPos pivot,
            @NotNull StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo currentBlockInfo,
            @NotNull StructurePlaceSettings data) {
        final BlockState blockState = currentBlockInfo.state();
        final BlockPos blockPos = currentBlockInfo.pos();

        if (blockState.isAir()) {
            final RandomSource random = data.getRandom(currentBlockInfo.pos());
            final ChunkAccess chunk = world.getChunk(blockPos);
            final BlockState floor = world.getBlockState(blockPos.below());
            addLeavesAbove(random, blockPos, floor, chunk);
        }

        return currentBlockInfo;
    }

    private void addLeavesAbove(RandomSource random, BlockPos top, BlockState floor, ChunkAccess chunk) {
        boolean flag = floor.is(Blocks.OAK_PLANKS) || floor.is(Blocks.BOOKSHELF) || floor.is(Blocks.OBSIDIAN);
        if (random.nextFloat() < probability && flag) {
            chunk.setBlockState(top,
                    Blocks.JUNGLE_LEAVES.defaultBlockState().trySetValue(LeavesBlock.PERSISTENT, Boolean.TRUE), false);
        }
    }

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return Services.PLATFORM.getOvergrowthProcessor();
    }
}