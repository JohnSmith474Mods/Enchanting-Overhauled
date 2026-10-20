package johnsmith.enchantingoverhauled.api.structure.processor.rule;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/**
 * A contract interface defining the block state outcome of an {@link OverhauledRule}.
 * <p>
 * Allows structure processor rules to output constant blocks ({@link ConstantOutput}),
 * weighted random blocks ({@link WeightedOutput}), or spatial gradients ({@link GradientOutput}).
 */
public interface RuleOutput {

    /**
     * Dispatch codec for serializing and deserializing any {@link RuleOutput} implementation
     * based on its {@code "type"} field.
     */
    Codec<RuleOutput> CODEC = Codec.STRING.dispatch(
            "type",
            RuleOutput::getTypeId,
            id -> switch (id) {
                case "constant" -> ConstantOutput.CODEC;
                case "weighted" -> WeightedOutput.CODEC;
                case "gradient" -> GradientOutput.CODEC;
                default -> throw new IllegalArgumentException("Unknown RuleOutput type: " + id);
            }
    );

    /**
     * Retrieves the unique string identifier for this output type.
     *
     * @return The type identifier (e.g., {@code "constant"}, {@code "weighted"}, or {@code "gradient"}).
     */
    String getTypeId();

    /**
     * Resolves the output {@link BlockState} for a matched block position.
     *
     * @param random      The per-block deterministic random source.
     * @param localPos    The unrotated template-local block position.
     * @param absolutePos The transformed world-space block position.
     * @param settings    The structure placement settings.
     * @return The resolved {@link BlockState}.
     */
    BlockState getOutput(RandomSource random, BlockPos localPos, BlockPos absolutePos, StructurePlaceSettings settings);
}