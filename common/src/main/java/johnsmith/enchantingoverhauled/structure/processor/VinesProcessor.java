package johnsmith.enchantingoverhauled.structure.processor;

import johnsmith.enchantingoverhauled.platform.Services;


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
 * A structure processor that attaches vines to the sides of solid blocks.
 * <p>
 * Iterates through blocks in the structure. If a block is solid, it checks adjacent horizontal
 * air spaces and randomly places vines on them, attaching back to the source block.
 */
public class VinesProcessor extends StructureProcessor {

    public static final MapCodec<VinesProcessor> CODEC = Codec.FLOAT
            .fieldOf("probability")
            .xmap(VinesProcessor::new, vinesProcessor -> vinesProcessor.probability);

    private final float probability;

    /**
     * Constructs a new VinesProcessor.
     *
     * @param probability The chance (0.0 - 1.0) to spawn vines on a valid face.
     */
    public VinesProcessor(float probability) {
        this.probability = probability;
    }

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

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return Services.PLATFORM.getVinesProcessor();
    }
}