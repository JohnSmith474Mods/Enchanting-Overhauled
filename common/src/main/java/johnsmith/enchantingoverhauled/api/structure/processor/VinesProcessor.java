package johnsmith.enchantingoverhauled.api.structure.processor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/**
 * A {@link StructureProcessor} that attaches vines ({@link Blocks#VINE}) to the sides of solid blocks.
 * <p>
 * Iterates through non-air blocks in the structure. If a block has a sturdy horizontal face adjacent
 * to an air block, it randomly places a vine in the adjacent air space oriented back toward the solid face.
 */
public class VinesProcessor extends StructureProcessor {

    /**
     * The {@link MapCodec} for serializing and deserializing a {@link VinesProcessor} from JSON.
     */
    public static final MapCodec<VinesProcessor> CODEC = Codec.FLOAT
            .fieldOf("probability")
            .xmap(VinesProcessor::new, vinesProcessor -> vinesProcessor.probability);

    /**
     * The probability ({@code 0.0} to {@code 1.0}) of spawning vines on each valid horizontal face.
     */
    private final float probability;

    /**
     * Constructs a new {@code VinesProcessor}.
     *
     * @param probability The chance ({@code 0.0} to {@code 1.0}) to spawn vines on a valid face.
     */
    public VinesProcessor(float probability) {
        this.probability = probability;
    }

    /**
     * Processes a single block during structure placement, attempting to attach vines to adjacent
     * horizontal air blocks if the current block is solid.
     *
     * @param levelReader       The level reader.
     * @param pos               The template offset position.
     * @param pivot             The structure pivot position.
     * @param originalBlockInfo The unrotated original block info from the template.
     * @param currentBlockInfo  The transformed block info in world coordinates.
     * @param data              The structure placement settings.
     * @return The unmodified {@code currentBlockInfo}.
     */
    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            @NotNull LevelReader levelReader,
            @NotNull BlockPos pos,
            @NotNull BlockPos pivot,
            @NotNull StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo currentBlockInfo,
            @NotNull StructurePlaceSettings data
    ) {
        final BlockState blockState = currentBlockInfo.state();
        final BlockPos blockPos = currentBlockInfo.pos();

        if (!blockState.isAir() && !blockState.is(Blocks.VINE)) {
            final RandomSource random = data.getRandom(currentBlockInfo.pos());
            final ChunkAccess chunk = levelReader.getChunk(blockPos);

            addVines(levelReader, random, blockPos, chunk);
        }
        return currentBlockInfo;
    }

    /**
     * Checks all four horizontal directions around {@code blockPos} and places a vine block in any
     * adjacent air block where {@code blockPos} has a sturdy face and the random roll succeeds.
     *
     * @param levelReader The level reader.
     * @param random      The deterministic per-block random source.
     * @param blockPos    The position of the solid anchor block.
     * @param chunk       The chunk access to mutate.
     */
    private void addVines(LevelReader levelReader, RandomSource random, BlockPos blockPos, ChunkAccess chunk) {
        BlockState blockState = levelReader.getBlockState(blockPos);

        if (!blockState.isAir() && !blockState.is(Blocks.VINE)) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                final BlockPos blockPos2 = blockPos.relative(direction);
                final BlockState blockState2 = levelReader.getBlockState(blockPos2);

                if (random.nextFloat() < this.probability && blockState2.isAir()) {
                    if (blockState.isFaceSturdy(levelReader, blockPos, direction)) {
                        BooleanProperty booleanProperty = VineBlock.getPropertyForFace(direction.getOpposite());
                        chunk.setBlockState(blockPos2, Blocks.VINE.defaultBlockState().trySetValue(booleanProperty, true), false);
                    }
                }
            }
        }
    }

    /**
     * Retrieves the registered {@link StructureProcessorType} for this processor.
     *
     * @return {@link StructureProcessors#ADD_VINES}.
     */
    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return StructureProcessors.ADD_VINES.get();
    }
}