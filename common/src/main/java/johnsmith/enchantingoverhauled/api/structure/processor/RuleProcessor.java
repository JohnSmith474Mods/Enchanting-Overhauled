package johnsmith.enchantingoverhauled.api.structure.processor;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import johnsmith.enchantingoverhauled.api.structure.processor.rule.OverhauledRule;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An enhanced rule-based {@link StructureProcessor} that evaluates a list of {@link OverhauledRule}s.
 * <p>
 * Improves upon vanilla's {@link net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor}
 * by seeding the per-block {@link RandomSource} using the actual transformed world block position
 * ({@code blockInfoOut.pos()}) rather than the structure pivot, supporting property copying
 * (such as stair facing/half or slab type), and supporting constant, weighted, and spatial gradient outputs.
 */
public class RuleProcessor extends StructureProcessor {

    /**
     * The {@link MapCodec} for serializing and deserializing a {@link RuleProcessor} from JSON.
     */
    public static final MapCodec<RuleProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    OverhauledRule.CODEC.listOf().fieldOf("rules").forGetter(p -> p.rules)
            ).apply(instance, RuleProcessor::new)
    );

    /**
     * The ordered list of {@link OverhauledRule}s evaluated for each block.
     */
    private final List<OverhauledRule> rules;

    /**
     * Constructs a new {@code RuleProcessor} with the given rules.
     *
     * @param rules The list of {@link OverhauledRule} instances to evaluate.
     */
    public RuleProcessor(List<OverhauledRule> rules) {
        this.rules = rules;
    }

    /**
     * Evaluates each {@link OverhauledRule} in order against the current block.
     * The first matching rule whose probability check succeeds determines the output {@link BlockState},
     * optionally copying compatible block state properties from {@code blockInfoOut.state()}.
     *
     * @param level        The level reader.
     * @param offset       The template offset position.
     * @param pivot        The structure pivot position.
     * @param blockInfoIn  The unrotated original block info from the template (local coordinates).
     * @param blockInfoOut The transformed block info in world coordinates (absolute coordinates).
     * @param settings     The structure placement settings.
     * @return The modified {@link StructureTemplate.StructureBlockInfo}, or {@code blockInfoOut} if no rule matched.
     */
    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pivot, StructureTemplate.StructureBlockInfo blockInfoIn, StructureTemplate.StructureBlockInfo blockInfoOut, StructurePlaceSettings settings) {
        // Fix 1: The third parameter is the structure pivot, not the block's position!
        // We must use blockInfoOut.pos() to get a deterministic random seed per block.
        RandomSource random = RandomSource.create(Mth.getSeed(blockInfoOut.pos()));

        BlockState inputState = blockInfoIn.state();
        BlockState locationState = blockInfoOut.state();

        for (OverhauledRule rule : this.rules) {
            // Fix 2: Pass the correct absolute position (blockInfoOut.pos()) to the test and output
            if (rule.test(inputState, locationState, blockInfoIn.pos(), blockInfoOut.pos(), pivot, random)) {
                if (random.nextFloat() < rule.probability()) {
                    BlockState newState = rule.output().getOutput(random, blockInfoIn.pos(), blockInfoOut.pos(), settings);

                    if (rule.copyProperties()) {
                        newState = copyProperties(locationState, newState);
                    }

                    return new StructureTemplate.StructureBlockInfo(blockInfoOut.pos(), newState, blockInfoOut.nbt());
                }
            }
        }

        return blockInfoOut;
    }

    /**
     * Copies all shared block state {@link Property} values from {@code oldState} onto {@code newState}.
     *
     * @param oldState The source {@link BlockState} whose properties should be copied.
     * @param newState The target {@link BlockState} receiving the matching properties.
     * @return The updated {@link BlockState}.
     */
    private static BlockState copyProperties(BlockState oldState, BlockState newState) {
        for (Property<?> property : oldState.getProperties()) {
            if (newState.hasProperty(property)) {
                newState = copyPropertySafely(oldState, newState, property);
            }
        }
        return newState;
    }

    /**
     * Type-safe helper to copy a single {@link Property} value from {@code oldState} to {@code newState}.
     *
     * @param <T>      The comparable value type of the property.
     * @param oldState The source block state.
     * @param newState The destination block state.
     * @param property The property to copy.
     * @return The destination block state with the property value applied.
     */
    private static <T extends Comparable<T>> BlockState copyPropertySafely(BlockState oldState, BlockState newState, Property<T> property) {
        return newState.setValue(property, oldState.getValue(property));
    }

    /**
     * Retrieves the registered {@link StructureProcessorType} for this processor.
     *
     * @return {@link StructureProcessors#RULE}.
     */
    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return StructureProcessors.RULE.get();
    }
}