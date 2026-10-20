package johnsmith.enchantingoverhauled.api.structure.processor.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import java.util.List;

/**
 * A {@link RuleOutput} implementation that selects a {@link BlockState} from an ordered palette
 * of {@link #states} based on the block's projected distance along a {@link SpatialVector} axis,
 * shaped by a {@link GradientCurve} and perturbed by random {@link #noise}.
 * <p>
 * Used extensively in structure processor lists (such as mossifying lower walls or weathering upper roofs)
 * to create natural vertical or directional block transitions.
 *
 * @param states      The ordered list of {@link BlockState}s from start ({@code 0.0}) to end ({@code 1.0}) of the gradient.
 * @param axis        The {@link SpatialVector} defining the local and/or global direction of the gradient (default: {@link SpatialVector#UP}).
 * @param minDistance The scalar projection distance corresponding to the start of the gradient ({@code t = 0.0}).
 * @param maxDistance The scalar projection distance corresponding to the end of the gradient ({@code t = 1.0}).
 * @param reverse     Whether to invert the normalized gradient fraction ({@code t = 1.0 - t}) after curve evaluation.
 * @param noise       The amplitude of uniform random jitter added to {@code t} for organic dithering between palette slices.
 * @param curve       The {@link GradientCurve} used to warp the normalized distance fraction {@code t}.
 */
public record GradientOutput(
        List<BlockState> states,
        SpatialVector axis,
        float minDistance,
        float maxDistance,
        boolean reverse,
        float noise,
        GradientCurve curve
) implements RuleOutput {

    /**
     * The {@link MapCodec} for serializing and deserializing a {@link GradientOutput} from JSON.
     */
    public static final MapCodec<GradientOutput> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BlockState.CODEC.listOf().fieldOf("states").forGetter(GradientOutput::states),
                    SpatialVector.CODEC.optionalFieldOf("axis", SpatialVector.UP).forGetter(GradientOutput::axis),
                    Codec.FLOAT.optionalFieldOf("min_distance", 0.0F).forGetter(GradientOutput::minDistance),
                    Codec.FLOAT.optionalFieldOf("max_distance", 15.0F).forGetter(GradientOutput::maxDistance),
                    Codec.BOOL.optionalFieldOf("reverse", false).forGetter(GradientOutput::reverse),
                    Codec.FLOAT.optionalFieldOf("noise", 0.0F).forGetter(GradientOutput::noise),
                    GradientCurve.CODEC.optionalFieldOf("curve", new GradientCurve.Linear()).forGetter(GradientOutput::curve)
            ).apply(instance, GradientOutput::new)
    );

    /**
     * {@inheritDoc}
     *
     * @return The type identifier {@code "gradient"}.
     */
    @Override
    public String getTypeId() {
        return "gradient";
    }

    /**
     * Evaluates the spatial projection, curve transformation, optional reversal, and dithering noise
     * for the given block coordinates, mapping the resulting fraction in {@code [0.0, 1.0]} to an
     * equal-width slice of {@link #states}.
     *
     * @param random      The per-block deterministic random source.
     * @param localPos    The unrotated template-local block position.
     * @param absolutePos The transformed world-space block position.
     * @param settings    The structure placement settings.
     * @return The selected {@link BlockState} from {@link #states}, or {@link Blocks#AIR} if the palette is empty.
     */
    @Override
    public BlockState getOutput(RandomSource random, BlockPos localPos, BlockPos absolutePos, StructurePlaceSettings settings) {
        if (this.states.isEmpty()) return Blocks.AIR.defaultBlockState();
        if (this.states.size() == 1) return this.states.get(0);

        // Calculate current position along the defined axis vector
        float distance = this.axis.dot(localPos, absolutePos);
        float range = this.maxDistance - this.minDistance;

        float t = 0.0F;
        if (range != 0.0F) {
            t = (distance - this.minDistance) / range;
        }

        // Apply curve transformation to bend the gradient density
        t = this.curve.apply(t);

        // Read the states list backwards if specified
        if (this.reverse) {
            t = 1.0F - t;
        }

        // Apply continuous noise for blending thresholds
        // Now that the Random is correctly block-based, this will correctly scatter!
        if (this.noise > 0.0F) {
            t += (random.nextFloat() - 0.5F) * this.noise;
        }

        // Clamp the final fraction securely into the 0.0 - 1.0 bounds
        t = Math.max(0.0F, Math.min(1.0F, t));

        // Fix 3: Map the fractional value using floor distribution
        // This guarantees an exactly equal slice of the gradient for every block state!
        int index = (int) Math.floor(t * this.states.size());
        index = Math.max(0, Math.min(this.states.size() - 1, index));

        return this.states.get(index);
    }
}