package johnsmith.structure.processor;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;

public class VinesProcessor extends StructureProcessor {
    public static final MapCodec<VinesProcessor> CODEC = Codec.FLOAT
            .fieldOf("probability")
            .xmap(VinesProcessor::new, vinesProcessor -> vinesProcessor.probability);

    private final float probability;

    public VinesProcessor(float probability) {
        this.probability = probability;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(
            WorldView world,
            BlockPos pos,
            BlockPos pivot,
            StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo currentBlockInfo,
            StructurePlacementData data) {
        final BlockState blockState = currentBlockInfo.state();
        final BlockPos blockPos = currentBlockInfo.pos();

        if (!blockState.isAir() && !blockState.isOf(Blocks.VINE)) {
            final Random random = data.getRandom(currentBlockInfo.pos());
            final Chunk chunk = world.getChunk(blockPos);
            addVines(world, random, blockPos, chunk);
        }

        return currentBlockInfo;
    }

    private void addVines(WorldView worldView, Random random, BlockPos blockPos, Chunk chunk) {
        BlockState blockState = worldView.getBlockState(blockPos);
        if (!blockState.isAir() && !blockState.isOf(Blocks.VINE)) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                final BlockPos blockPos2 = blockPos.offset(direction);
                final BlockState blockState2 = worldView.getBlockState(blockPos2);
                if (random.nextFloat() < this.probability && blockState2.isAir()) {
                    if (blockState.isSideSolidFullSquare(worldView, blockPos, direction)) {
                        BooleanProperty booleanProperty = VineBlock.getFacingProperty(direction.getOpposite());
                        chunk.setBlockState(blockPos2, Blocks.VINE.getDefaultState().with(booleanProperty, true), false);
                    }
                }
            }
        }
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ProcessorRegistry.VINES_PROCESSOR;
    }
}