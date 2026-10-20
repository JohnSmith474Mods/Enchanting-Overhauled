package johnsmith.enchantingoverhauled.api.structure.processor.rule;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/**
 * A {@link RuleOutput} implementation that always returns a single fixed {@link BlockState}.
 *
 * @param state The constant {@link BlockState} to output when the parent {@link OverhauledRule} matches.
 */
public record ConstantOutput(BlockState state) implements RuleOutput {

    /**
     * The {@link MapCodec} for serializing and deserializing a {@link ConstantOutput} from JSON.
     */
    public static final MapCodec<ConstantOutput> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BlockState.CODEC.fieldOf("state").forGetter(ConstantOutput::state)
            ).apply(instance, ConstantOutput::new)
    );

    /**
     * {@inheritDoc}
     *
     * @return The type identifier {@code "constant"}.
     */
    @Override
    public String getTypeId() {
        return "constant";
    }

    /**
     * Returns the constant {@link #state}.
     *
     * @param random      The per-block random source (unused).
     * @param localPos    The unrotated template-local block position (unused).
     * @param absolutePos The world-space block position (unused).
     * @param settings    The structure placement settings (unused).
     * @return The fixed {@link BlockState}.
     */
    @Override
    public BlockState getOutput(RandomSource random, BlockPos localPos, BlockPos absolutePos, StructurePlaceSettings settings) {
        return this.state;
    }
}