package johnsmith.enchantingoverhauled.api.structure.processor;

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
 * A {@link StructureProcessor} that adds foliage ({@link Blocks#JUNGLE_LEAVES}) to generated structures.
 * <p>
 * Inspects air blocks in the structure template and checks the block directly below them.
 * If the floor block is Oak Planks, a Bookshelf, or Obsidian, it has a configurable {@link #probability}
 * to place persistent Jungle Leaves at the current air position.
 */
public class OvergrowthProcessor extends StructureProcessor {

    /**
     * The {@link MapCodec} for serializing and deserializing an {@link OvergrowthProcessor} from JSON.
     */
    public static final MapCodec<OvergrowthProcessor> CODEC = Codec.FLOAT
            .fieldOf("probability")
            .xmap(OvergrowthProcessor::new, overgrowthProcessor -> overgrowthProcessor.probability);

    /**
     * The probability ({@code 0.0} to {@code 1.0}) of spawning leaves above an eligible floor block.
     */
    private final float probability;

    /**
     * Constructs a new {@code OvergrowthProcessor}.
     *
     * @param probability The chance ({@code 0.0} to {@code 1.0}) to spawn leaves above a valid block.
     */
    public OvergrowthProcessor(float probability) {
        this.probability = probability;
    }

    /**
     * Processes a single block during structure template placement, potentially placing persistent
     * Jungle Leaves in air spaces above eligible floor blocks.
     *
     * @param world             The level reader.
     * @param pos               The template offset position.
     * @param pivot             The structure pivot position.
     * @param originalBlockInfo The unrotated original block info from the template.
     * @param currentBlockInfo  The transformed block info in world coordinates.
     * @param data              The structure placement settings.
     * @return The resulting {@link StructureTemplate.StructureBlockInfo}.
     */
    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            @NotNull LevelReader world,
            @NotNull BlockPos pos,
            @NotNull BlockPos pivot,
            @NotNull StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo currentBlockInfo,
            @NotNull StructurePlaceSettings data
    ) {
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

    /**
     * Places persistent Jungle Leaves at {@code top} if {@code floor} is Oak Planks, Bookshelf, or Obsidian
     * and the random roll succeeds.
     *
     * @param random The deterministic block random source.
     * @param top    The air position above the floor.
     * @param floor  The block state directly below {@code top}.
     * @param chunk  The chunk access to mutate.
     */
    private void addLeavesAbove(RandomSource random, BlockPos top, BlockState floor, ChunkAccess chunk) {
        boolean flag = floor.is(Blocks.OAK_PLANKS) || floor.is(Blocks.BOOKSHELF) || floor.is(Blocks.OBSIDIAN);
        if (random.nextFloat() < probability && flag) {
            chunk.setBlockState(top,
                    Blocks.JUNGLE_LEAVES.defaultBlockState().trySetValue(LeavesBlock.PERSISTENT, Boolean.TRUE), false);
        }
    }

    /**
     * Retrieves the registered {@link StructureProcessorType} for this processor.
     *
     * @return {@link StructureProcessors#OVERGROWTH}.
     */
    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return StructureProcessors.OVERGROWTH.get();
    }
}