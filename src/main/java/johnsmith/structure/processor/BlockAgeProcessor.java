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

public class BlockAgeProcessor extends StructureProcessor {
    public static final MapCodec<BlockAgeProcessor> CODEC = Codec.FLOAT
            .fieldOf("mossiness")
            .xmap(BlockAgeProcessor::new, blockAgeProcessors -> blockAgeProcessors.mossiness);

    private static final float PROBABILITY_STONE = 0.75F;
    private static final float PROBABILITY_BRICKS = 0.5F;
    private static final float PROBABILITY_OBSIDIAN = 0.15F;
    private static final BlockState[] STONE_REPLACEMENTS = {
            Blocks.ANDESITE.getDefaultState(), Blocks.ANDESITE.getDefaultState(),
            Blocks.ANDESITE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(),
            Blocks.COBBLESTONE.getDefaultState(), Blocks.GRAVEL.getDefaultState()
    };
    private static final BlockState[] STONE_BRICK_REPLACEMENTS = {
            Blocks.CRACKED_STONE_BRICKS.getDefaultState()
    };
    private static final BlockState[] MOSSY_STONE_BRICK_REPLACEMENTS = {
            Blocks.CRACKED_STONE_BRICKS.getDefaultState()
    };
    private static final BlockState[] STONE_REPLACEMENTS_MOSSY = { Blocks.MOSSY_COBBLESTONE.getDefaultState() };
    private final float mossiness;

    public BlockAgeProcessor(float blockAge) {
        this.mossiness = blockAge;
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
        if (blockState.isOf(Blocks.STONE_STAIRS)) {
            blockState1 = this.replaceStoneStairs(random, blockState);
        } else if (blockState.isOf(Blocks.STONE_SLAB)) {
            blockState1 = this.replaceStoneSlabs(random, blockState);
        } else if (blockState.isOf(Blocks.STONE)) {
            blockState1 = this.replaceStoneBlocks(random);
        } else if (blockState.isOf(Blocks.STONE_BRICK_SLAB)) {
            blockState1 = this.replaceStoneBrickSlabs(random, blockState);
        } else if (blockState.isOf(Blocks.STONE_BRICKS)) {
            blockState1 = this.replaceStoneBrickBlocks(random);
        } else if (blockState.isOf(Blocks.OBSIDIAN)) {
            blockState1 = this.maybeReplacObsidian(random);
        }
        return blockState1 != null
                ? new StructureTemplate.StructureBlockInfo(blockPos, blockState1, currentBlockInfo.nbt())
                : currentBlockInfo;
    }

    @Nullable
    private BlockState replaceStoneBlocks(Random random) {
        if (random.nextFloat() >= PROBABILITY_STONE)
            return null;
        return this.getRandomBlock(random, STONE_REPLACEMENTS, STONE_REPLACEMENTS_MOSSY);
    }

    @Nullable
    private BlockState replaceStoneSlabs(Random random, BlockState blockState) {
        if (random.nextFloat() >= PROBABILITY_STONE)
            return null;
        SlabType type = blockState.get(SlabBlock.TYPE);
        BlockState[] slabs = new BlockState[] {
                Blocks.ANDESITE_SLAB.getDefaultState().with(SlabBlock.TYPE, type),
                Blocks.ANDESITE_SLAB.getDefaultState().with(SlabBlock.TYPE, type),
                Blocks.ANDESITE_SLAB.getDefaultState().with(SlabBlock.TYPE, type),
                Blocks.COBBLESTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, type),
                Blocks.COBBLESTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, type)
        };
        BlockState[] mossy_slabs = new BlockState[] {
                Blocks.MOSSY_COBBLESTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, type)
        };
        return getRandomBlock(random, slabs, mossy_slabs);
    }

    @Nullable
    private BlockState replaceStoneStairs(Random random, BlockState blockState) {
        if (random.nextFloat() >= PROBABILITY_STONE)
            return null;
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        BlockHalf half = blockState.get(Properties.BLOCK_HALF);
        BlockState[] stairs = new BlockState[] {
                Blocks.COBBLESTONE_STAIRS.getDefaultState()
                        .with(Properties.HORIZONTAL_FACING, direction)
                        .with(Properties.BLOCK_HALF, half),
                Blocks.ANDESITE_STAIRS.getDefaultState()
                        .with(Properties.HORIZONTAL_FACING, direction)
                        .with(Properties.BLOCK_HALF, half)
        };
        BlockState[] mossy_stairs = new BlockState[] {
                Blocks.MOSSY_COBBLESTONE_STAIRS.getDefaultState()
                        .with(Properties.HORIZONTAL_FACING, direction)
                        .with(Properties.BLOCK_HALF, half)
        };
        return this.getRandomBlock(random, stairs, mossy_stairs);
    }

    @Nullable
    private BlockState replaceStoneBrickBlocks(Random random) {
        if (random.nextFloat() >= PROBABILITY_BRICKS)
            return null;
        return this.getRandomBlock(random, STONE_BRICK_REPLACEMENTS, MOSSY_STONE_BRICK_REPLACEMENTS);
    }

    @Nullable
    private BlockState replaceStoneBrickSlabs(Random random, BlockState blockState) {
        if (random.nextFloat() >= PROBABILITY_BRICKS)
            return null;
        SlabType type = blockState.get(SlabBlock.TYPE);
        BlockState[] slabs = new BlockState[] {
                Blocks.STONE_BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, type)
        };
        BlockState[] mossy_slabs = new BlockState[] {
                Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, type)
        };
        return getRandomBlock(random, slabs, mossy_slabs);
    }

    @Nullable
    private BlockState maybeReplacObsidian(Random random) {
        return random.nextFloat() < PROBABILITY_OBSIDIAN ? Blocks.CRYING_OBSIDIAN.getDefaultState()
                : null;
    }

    private BlockState getRandomBlock(Random random, BlockState[] blockState1, BlockState[] blockState2) {
        return random.nextFloat() < this.mossiness ? getRandomBlock(random, blockState2)
                : getRandomBlock(random, blockState1);
    }

    private static BlockState getRandomBlock(Random random, BlockState[] blockState) {
        return blockState[random.nextInt(blockState.length)];
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ProcessorRegistry.BLOCK_AGE_PROCESSOR;
    }
}