package johnsmith.structure.processor;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;

public class OvergrowthProcessor extends StructureProcessor {

    public static final MapCodec<OvergrowthProcessor> CODEC = Codec.FLOAT
            .fieldOf("probability")
            .xmap(OvergrowthProcessor::new, overgrowthProcessor -> overgrowthProcessor.probability);
    private final float probability;

    public OvergrowthProcessor(float probability) {
        this.probability = probability;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos,
                                                        BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo,
                                                        StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        final BlockState blockState = currentBlockInfo.state();
        final BlockPos blockPos = currentBlockInfo.pos();

        if (blockState.isAir()) {
            final Random random = data.getRandom(currentBlockInfo.pos());
            final Chunk chunk = world.getChunk(blockPos);
            final BlockState floor = world.getBlockState(blockPos.down());
            addLeavesAbove(random, blockPos, floor, chunk);
        }

        return currentBlockInfo;
    }

    private void addLeavesAbove(Random random, BlockPos top, BlockState floor, Chunk chunk) {
        boolean flag = floor.isOf(Blocks.OAK_PLANKS) || floor.isOf(Blocks.BOOKSHELF) || floor.isOf(Blocks.OBSIDIAN);
        if (random.nextFloat() < probability && flag) {
            chunk.setBlockState(top,
                    Blocks.JUNGLE_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, Boolean.TRUE), false);
        }
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ProcessorRegistry.OVERGROWTH_PROCESSOR;
    }
}