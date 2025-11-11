package johnsmith.structure.processor;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

public class DesertifyProcessor extends StructureProcessor {
    public static final MapCodec<DesertifyProcessor> CODEC = Codec.BOOL
            .fieldOf("desertify")
            .xmap(DesertifyProcessor::new, desertifier -> desertifier.desertify);

    private static final float RUINED = 0.33F;
    private static final float PROBABILITY_OBSIDIAN = 0.15F;
    private static final BlockState[] SANDSTONE = {
            Blocks.SANDSTONE.getDefaultState(),
            Blocks.SAND.getDefaultState()
    };
    private static final BlockState[] CUT_SANDSTONE = { Blocks.CUT_SANDSTONE.getDefaultState() };
    private static final BlockState[] SMOOTH_SANDSTONE = { Blocks.SMOOTH_SANDSTONE.getDefaultState() };
    private final Boolean desertify;

    public DesertifyProcessor(boolean bool) {
        this.desertify = bool;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos,
                                                        BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo,
                                                        StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        final BlockPos blockPos = currentBlockInfo.pos();
        final BlockState blockState = currentBlockInfo.state();
        BlockState blockState1 = null;
        final Random random = data.getRandom(blockPos);
        // replace stone
        if (blockState.isOf(Blocks.STONE_STAIRS)) {
            blockState1 = this.replaceStoneStairs(random, blockState);
        } else if (blockState.isOf(Blocks.STONE_SLAB)) {
            blockState1 = this.replaceStoneSlabs(random, blockState);
        } else if (blockState.isOf(Blocks.STONE)) {
            blockState1 = this.getRandomBlock(random, SMOOTH_SANDSTONE, SANDSTONE);
        } // replace andesite
        else if (blockState.isOf(Blocks.POLISHED_ANDESITE_STAIRS)) {
            blockState1 = this.replaceAndesiteStairs(random, blockState);
        } else if (blockState.isOf(Blocks.POLISHED_ANDESITE_SLAB)) {
            blockState1 = this.replaceAndesiteSlabs(random, blockState);
        } else if (blockState.isOf(Blocks.POLISHED_ANDESITE)) {
            blockState1 = CUT_SANDSTONE[0];
        } // replace stone bricks
        else if (blockState.isOf(Blocks.STONE_BRICK_STAIRS)) {
            blockState1 = this.replaceAndesiteStairs(random, blockState);
        } else if (blockState.isOf(Blocks.STONE_BRICK_SLAB)) {
            blockState1 = this.replaceStoneBrickSlabs(random, blockState);
        } else if (blockState.isOf(Blocks.STONE_BRICKS)) {
            blockState1 = SANDSTONE[0];
        } // replace obsidian
        else if (blockState.isOf(Blocks.OBSIDIAN)) {
            blockState1 = this.maybeReplacObsidian(random);
        }
        return blockState1 != null
                ? new StructureTemplate.StructureBlockInfo(blockPos, blockState1, currentBlockInfo.nbt())
                : currentBlockInfo;
    }

    @Nullable
    private BlockState replaceStoneSlabs(Random random, BlockState blockState) {
        SlabType type = blockState.get(SlabBlock.TYPE);
        BlockState[] smooth_sandstone_slabs = new BlockState[] {
                Blocks.SMOOTH_SANDSTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, type)
        };
        BlockState[] sandstone_slabs = new BlockState[] {
                Blocks.SANDSTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, type)
        };
        return getRandomBlock(random, smooth_sandstone_slabs, sandstone_slabs);
    }

    @Nullable
    private BlockState replaceStoneStairs(Random random, BlockState blockState) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        BlockHalf half = blockState.get(Properties.BLOCK_HALF);
        BlockState[] stairs = new BlockState[] {
                Blocks.SMOOTH_SANDSTONE_STAIRS.getDefaultState()
                        .with(Properties.HORIZONTAL_FACING, direction)
                        .with(Properties.BLOCK_HALF, half)
        };
        BlockState[] mossy_stairs = new BlockState[] {
                Blocks.SANDSTONE_STAIRS.getDefaultState()
                        .with(Properties.HORIZONTAL_FACING, direction)
                        .with(Properties.BLOCK_HALF, half)
        };
        return this.getRandomBlock(random, stairs, mossy_stairs);
    }

    @Nullable
    private BlockState replaceAndesiteSlabs(Random random, BlockState blockState) {
        SlabType type = blockState.get(SlabBlock.TYPE);
        return Blocks.CUT_SANDSTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, type);
    }

    @Nullable
    private BlockState replaceAndesiteStairs(Random random, BlockState blockState) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        BlockHalf half = blockState.get(Properties.BLOCK_HALF);
        return Blocks.SANDSTONE_STAIRS.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, direction)
                .with(Properties.BLOCK_HALF, half);
    }

    @Nullable
    private BlockState replaceStoneBrickSlabs(Random random, BlockState blockState) {
        SlabType type = blockState.get(SlabBlock.TYPE);
        return Blocks.SANDSTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, type);
    }

    @Nullable
    private BlockState maybeReplacObsidian(Random random) {
        return random.nextFloat() < PROBABILITY_OBSIDIAN ? Blocks.CRYING_OBSIDIAN.getDefaultState()
                : null;
    }

    private BlockState getRandomBlock(Random random, BlockState[] blockState1, BlockState[] blockState2) {
        return random.nextFloat() < RUINED ? getRandomBlock(random, blockState2)
                : getRandomBlock(random, blockState1);
    }

    private static BlockState getRandomBlock(Random random, BlockState[] blockState) {
        return blockState[random.nextInt(blockState.length)];
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ProcessorRegistry.DESERT_PROCESSOR;
    }
}