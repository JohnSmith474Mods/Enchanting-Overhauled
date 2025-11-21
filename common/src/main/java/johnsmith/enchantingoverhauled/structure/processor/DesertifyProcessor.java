package johnsmith.enchantingoverhauled.structure.processor;

import johnsmith.enchantingoverhauled.platform.Services;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/**
 * A structure processor that converts stone and other materials into sand-based variants.
 * <p>
 * This is used to adapt standard library ruins for desert environments.
 * It replaces:
 * <ul>
 * <li>Stone/Stone Bricks -> Smooth Sandstone, Sandstone.</li>
 * <li>Andesite -> Cut Sandstone.</li>
 * <li>Stairs/Slabs -> Sandstone equivalents.</li>
 * <li>Obsidian -> Crying Obsidian (rarely).</li>
 * </ul>
 */
public class DesertifyProcessor extends StructureProcessor {

    public static final MapCodec<DesertifyProcessor> CODEC = Codec.BOOL
            .fieldOf("desertify")
            .xmap(DesertifyProcessor::new, desertifier -> desertifier.desertify);

    private static final float RUINED = 0.33F;
    private static final float PROBABILITY_OBSIDIAN = 0.15F;
    private static final BlockState[] SANDSTONE = {
            Blocks.SANDSTONE.defaultBlockState(),
            Blocks.SAND.defaultBlockState()
    };
    private static final BlockState[] CUT_SANDSTONE = { Blocks.CUT_SANDSTONE.defaultBlockState() };
    private static final BlockState[] SMOOTH_SANDSTONE = { Blocks.SMOOTH_SANDSTONE.defaultBlockState() };

    private final Boolean desertify;

    /**
     * Constructs a new DesertifyProcessor.
     *
     * @param bool Whether to enable the processor (legacy parameter, currently unused logic-wise).
     */
    public DesertifyProcessor(boolean bool) {
        this.desertify = bool;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            @NotNull LevelReader world,
            @NotNull BlockPos pos,
            @NotNull BlockPos pivot,
            @NotNull StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo currentBlockInfo,
            StructurePlaceSettings data
    ) {
        final BlockPos blockPos = currentBlockInfo.pos();
        final BlockState blockState = currentBlockInfo.state();
        BlockState blockState1 = null;
        final RandomSource random = data.getRandom(blockPos);

        // replace stone
        if (blockState.is(Blocks.STONE_STAIRS)) {
            blockState1 = this.replaceStoneStairs(random, blockState);
        } else if (blockState.is(Blocks.STONE_SLAB)) {
            blockState1 = this.replaceStoneSlabs(random, blockState);
        } else if (blockState.is(Blocks.STONE)) {
            blockState1 = this.getRandomBlock(random, SMOOTH_SANDSTONE, SANDSTONE);
        }
        // replace andesite
        else if (blockState.is(Blocks.POLISHED_ANDESITE_STAIRS)) {
            blockState1 = this.replaceAndesiteStairs(random, blockState);
        } else if (blockState.is(Blocks.POLISHED_ANDESITE_SLAB)) {
            blockState1 = this.replaceAndesiteSlabs(random, blockState);
        } else if (blockState.is(Blocks.POLISHED_ANDESITE)) {
            blockState1 = CUT_SANDSTONE[0];
        }
        // replace stone bricks
        else if (blockState.is(Blocks.STONE_BRICK_STAIRS)) {
            blockState1 = this.replaceAndesiteStairs(random, blockState);
        } else if (blockState.is(Blocks.STONE_BRICK_SLAB)) {
            blockState1 = this.replaceStoneBrickSlabs(random, blockState);
        } else if (blockState.is(Blocks.STONE_BRICKS)) {
            blockState1 = SANDSTONE[0];
        }
        // replace obsidian
        else if (blockState.is(Blocks.OBSIDIAN)) {
            blockState1 = this.maybeReplacObsidian(random);
        }
        return blockState1 != null
                ? new StructureTemplate.StructureBlockInfo(blockPos, blockState1, currentBlockInfo.nbt())
                : currentBlockInfo;
    }

    @Nullable
    private BlockState replaceStoneSlabs(RandomSource random, BlockState blockState) {
        SlabType type = blockState.getValue(SlabBlock.TYPE);
        BlockState[] smooth_sandstone_slabs = new BlockState[] {
                Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type)
        };
        BlockState[] sandstone_slabs = new BlockState[] {
                Blocks.SANDSTONE_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type)
        };
        return getRandomBlock(random, smooth_sandstone_slabs, sandstone_slabs);
    }

    @Nullable
    private BlockState replaceStoneStairs(RandomSource random, BlockState blockState) {
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Half half = blockState.getValue(BlockStateProperties.HALF);
        BlockState[] stairs = new BlockState[] {
                Blocks.SMOOTH_SANDSTONE_STAIRS.defaultBlockState()
                        .trySetValue(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .trySetValue(BlockStateProperties.HALF, half)
        };
        BlockState[] mossy_stairs = new BlockState[] {
                Blocks.SANDSTONE_STAIRS.defaultBlockState()
                        .trySetValue(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .trySetValue(BlockStateProperties.HALF, half)
        };
        return this.getRandomBlock(random, stairs, mossy_stairs);
    }

    private @NotNull BlockState replaceAndesiteSlabs(RandomSource random, BlockState blockState) {
        SlabType type = blockState.getValue(SlabBlock.TYPE);
        return Blocks.CUT_SANDSTONE_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type);
    }

    private @NotNull BlockState replaceAndesiteStairs(RandomSource random, BlockState blockState) {
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Half half = blockState.getValue(BlockStateProperties.HALF);
        return Blocks.SANDSTONE_STAIRS.defaultBlockState()
                .trySetValue(BlockStateProperties.HORIZONTAL_FACING, direction)
                .trySetValue(BlockStateProperties.HALF, half);
    }

    private @NotNull BlockState replaceStoneBrickSlabs(RandomSource random, BlockState blockState) {
        SlabType type = blockState.getValue(SlabBlock.TYPE);
        return Blocks.SANDSTONE_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type);
    }

    @Nullable
    private BlockState maybeReplacObsidian(RandomSource random) {
        return random.nextFloat() < PROBABILITY_OBSIDIAN ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                : null;
    }

    private BlockState getRandomBlock(RandomSource random, BlockState[] blockState1, BlockState[] blockState2) {
        return random.nextFloat() < RUINED ? getRandomBlock(random, blockState2)
                : getRandomBlock(random, blockState1);
    }

    private static BlockState getRandomBlock(RandomSource random, BlockState[] blockState) {
        return blockState[random.nextInt(blockState.length)];
    }

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return Services.PLATFORM.getDesertProcessor();
    }
}