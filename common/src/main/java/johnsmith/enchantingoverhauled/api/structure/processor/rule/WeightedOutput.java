package johnsmith.enchantingoverhauled.api.structure.processor.rule;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/**
 * A {@link RuleOutput} implementation that randomly selects a {@link BlockState} from a weighted
 * list of candidates ({@link SimpleWeightedRandomList}).
 *
 * @param outputs A {@link SimpleWeightedRandomList} of candidate {@link BlockState}s and their integer weights.
 */
public record WeightedOutput(SimpleWeightedRandomList<BlockState> outputs) implements RuleOutput {

    /**
     * The {@link MapCodec} for serializing and deserializing a {@link WeightedOutput} from JSON.
     */
    public static final MapCodec<WeightedOutput> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    SimpleWeightedRandomList.wrappedCodec(BlockState.CODEC).fieldOf("outputs").forGetter(WeightedOutput::outputs)
            ).apply(instance, WeightedOutput::new)
    );

    /**
     * {@inheritDoc}
     *
     * @return The type identifier {@code "weighted"}.
     */
    @Override
    public String getTypeId() {
        return "weighted";
    }

    /**
     * Draws a random {@link BlockState} from {@link #outputs} using the per-block {@link RandomSource},
     * falling back to {@link Blocks#AIR} if the weighted list is empty.
     *
     * @param random      The per-block deterministic random source.
     * @param localPos    The unrotated template-local block position (unused).
     * @param absolutePos The transformed world-space block position (unused).
     * @param settings    The structure placement settings (unused).
     * @return The randomly selected {@link BlockState}.
     */
    @Override
    public BlockState getOutput(RandomSource random, BlockPos localPos, BlockPos absolutePos, StructurePlaceSettings settings) {
        return this.outputs.getRandomValue(random).orElse(Blocks.AIR.defaultBlockState());
    }
}