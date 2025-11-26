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
 * A structure processor that "ages" blocks by replacing them with worn or mossy variants.
 * <p>
 * This processor iterates through the blocks in a structure template and randomly replaces:
 * <ul>
 * <li>Stone -> Andesite, Cobblestone, Gravel, or Mossy Cobblestone.</li>
 * <li>Stone Bricks -> Cracked or Mossy Stone Bricks.</li>
 * <li>Stone Slabs/Stairs -> Andesite, Cobblestone, or Mossy variants.</li>
 * <li>Obsidian -> Crying Obsidian.</li>
 * </ul>
 * The intensity of the aging is controlled by the {@code mossiness} parameter.
 */
public class BlockAgeProcessor extends StructureProcessor {

    /**
     * The Codec for serializing and deserializing this processor.
     */
    public static final MapCodec<BlockAgeProcessor> CODEC = Codec.FLOAT
            .fieldOf("mossiness")
            .xmap(BlockAgeProcessor::new, blockAgeProcessors -> blockAgeProcessors.mossiness);

    private static final float PROBABILITY_STONE = .75F;
    private static final float PROBABILITY_BRICKS = .5F;
    private static final float PROBABILITY_OBSIDIAN = .15F;

    private static final BlockState[] STONE_REPLACEMENTS = {
            Blocks.ANDESITE.defaultBlockState(), Blocks.ANDESITE.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState()
    };
    private static final BlockState[] STONE_BRICK_REPLACEMENTS = {
            Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
    };
    private static final BlockState[] MOSSY_STONE_BRICK_REPLACEMENTS = {
            Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
    };
    private static final BlockState[] STONE_REPLACEMENTS_MOSSY = { Blocks.MOSSY_COBBLESTONE.defaultBlockState() };

    private final float mossiness;

    /**
     * Constructs a new BlockAgeProcessor.
     *
     * @param blockAge The chance (0.0 - 1.0) that a block will be replaced with a mossy/worn variant.
     */
    public BlockAgeProcessor(float blockAge) {
        this.mossiness = blockAge;
    }

    /**
     * Processes a single block in the structure to potentially replace it.
     *
     * @param world             The world reader.
     * @param pos               The position of the structure.
     * @param pivot             The pivot position.
     * @param originalBlockInfo The original block info from the template.
     * @param currentBlockInfo  The current block info (after previous processors).
     * @param data              The placement settings.
     * @return The modified block info, or the original if no change occurred.
     */
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

        if (blockState.is(Blocks.STONE_STAIRS)) {
            blockState1 = this.replaceStoneStairs(random, blockState);
        } else if (blockState.is(Blocks.STONE_SLAB)) {
            blockState1 = this.replaceStoneSlabs(random, blockState);
        } else if (blockState.is(Blocks.STONE)) {
            blockState1 = this.replaceStoneBlocks(random);
        } else if (blockState.is(Blocks.STONE_BRICK_SLAB)) {
            blockState1 = this.replaceStoneBrickSlabs(random, blockState);
        } else if (blockState.is(Blocks.STONE_BRICKS)) {
            blockState1 = this.replaceStoneBrickBlocks(random);
        } else if (blockState.is(Blocks.OBSIDIAN)) {
            blockState1 = this.maybeReplacObsidian(random);
        }
        return blockState1 != null
                ? new StructureTemplate.StructureBlockInfo(blockPos, blockState1, currentBlockInfo.nbt())
                : currentBlockInfo;
    }

    @Nullable
    private BlockState replaceStoneBlocks(RandomSource random) {
        if (random.nextFloat() >= PROBABILITY_STONE)
            return null;
        return this.getRandomBlock(random, STONE_REPLACEMENTS, STONE_REPLACEMENTS_MOSSY);
    }

    @Nullable
    private BlockState replaceStoneSlabs(RandomSource random, BlockState blockState) {
        if (random.nextFloat() >= PROBABILITY_STONE)
            return null;
        SlabType type = blockState.getValue(SlabBlock.TYPE);
        BlockState[] slabs = new BlockState[] {
                Blocks.ANDESITE_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type),
                Blocks.ANDESITE_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type),
                Blocks.ANDESITE_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type),
                Blocks.COBBLESTONE_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type),
                Blocks.COBBLESTONE_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type)
        };
        BlockState[] mossy_slabs = new BlockState[] {
                Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type)
        };
        return getRandomBlock(random, slabs, mossy_slabs);
    }

    @Nullable
    private BlockState replaceStoneStairs(RandomSource random, BlockState blockState) {
        if (random.nextFloat() >= PROBABILITY_STONE)
            return null;
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Half half = blockState.getValue(BlockStateProperties.HALF);
        BlockState[] stairs = new BlockState[] {
                Blocks.COBBLESTONE_STAIRS.defaultBlockState()
                        .trySetValue(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .trySetValue(BlockStateProperties.HALF, half),
                Blocks.ANDESITE_STAIRS.defaultBlockState()
                        .trySetValue(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .trySetValue(BlockStateProperties.HALF, half)
        };
        BlockState[] mossy_stairs = new BlockState[] {
                Blocks.MOSSY_COBBLESTONE_STAIRS.defaultBlockState()
                        .trySetValue(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .trySetValue(BlockStateProperties.HALF, half)
        };
        return this.getRandomBlock(random, stairs, mossy_stairs);
    }

    @Nullable
    private BlockState replaceStoneBrickBlocks(RandomSource random) {
        if (random.nextFloat() >= PROBABILITY_BRICKS)
            return null;
        return this.getRandomBlock(random, STONE_BRICK_REPLACEMENTS, MOSSY_STONE_BRICK_REPLACEMENTS);
    }

    @Nullable
    private BlockState replaceStoneBrickSlabs(RandomSource random, BlockState blockState) {
        if (random.nextFloat() >= PROBABILITY_BRICKS)
            return null;
        SlabType type = blockState.getValue(SlabBlock.TYPE);
        BlockState[] slabs = new BlockState[] {
                Blocks.STONE_BRICK_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type)
        };
        BlockState[] mossy_slabs = new BlockState[] {
                Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState().trySetValue(SlabBlock.TYPE, type)
        };
        return getRandomBlock(random, slabs, mossy_slabs);
    }

    @Nullable
    private BlockState maybeReplacObsidian(RandomSource random) {
        return random.nextFloat() < PROBABILITY_OBSIDIAN ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                : null;
    }

    private BlockState getRandomBlock(RandomSource random, BlockState[] blockState1, BlockState[] blockState2) {
        return random.nextFloat() < this.mossiness ? getRandomBlock(random, blockState2)
                : getRandomBlock(random, blockState1);
    }

    private static BlockState getRandomBlock(RandomSource random, BlockState[] blockState) {
        return blockState[random.nextInt(blockState.length)];
    }

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return Services.PLATFORM.getBlockAgeProcessor();
    }
}