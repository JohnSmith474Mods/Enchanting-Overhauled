package johnsmith.enchantingoverhauled.api.structure.processor.rule;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.List;

/**
 * A data-driven rule evaluated by {@link johnsmith.enchantingoverhauled.api.structure.processor.RuleProcessor}.
 * <p>
 * Supports either single or array-based {@code input_predicate} and {@code location_predicate} definitions
 * (evaluated via logical OR within each list), an optional {@link PosRuleTest}, a rule-level trigger
 * {@link #probability}, a {@link #copyProperties} flag for preserving block state properties (such as
 * stair orientation or slab half), and a polymorphic {@link RuleOutput}.
 *
 * @param inputPredicates    List of {@link RuleTest} predicates tested against the template's original input block state (any match passes).
 * @param locationPredicates List of {@link RuleTest} predicates tested against the current block state at the target location (any match passes).
 * @param positionPredicate  A {@link PosRuleTest} evaluated against the local, absolute, and pivot positions.
 * @param probability        The probability ({@code 0.0} to {@code 1.0}) that this rule applies when all predicates match.
 * @param copyProperties     Whether shared block state properties should be copied from the existing state onto the output state.
 * @param output             The {@link RuleOutput} provider that produces the replacement {@link BlockState}.
 */
public record OverhauledRule(
        List<RuleTest> inputPredicates,
        List<RuleTest> locationPredicates,
        PosRuleTest positionPredicate,
        float probability,
        boolean copyProperties,
        RuleOutput output
) {

    /**
     * Flexible codec that accepts either a single {@link RuleTest} object or a JSON array of {@link RuleTest} objects.
     */
    private static final Codec<List<RuleTest>> PREDICATE_LIST_CODEC = Codec.either(RuleTest.CODEC, RuleTest.CODEC.listOf())
            .xmap(
                    either -> either.map(List::of, list -> list),
                    list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)
            );

    /**
     * The codec responsible for serializing and deserializing {@link OverhauledRule} instances from JSON.
     */
    public static final Codec<OverhauledRule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    PREDICATE_LIST_CODEC.optionalFieldOf("input_predicate", List.of(AlwaysTrueTest.INSTANCE)).forGetter(OverhauledRule::inputPredicates),
                    PREDICATE_LIST_CODEC.optionalFieldOf("location_predicate", List.of(AlwaysTrueTest.INSTANCE)).forGetter(OverhauledRule::locationPredicates),
                    PosRuleTest.CODEC.optionalFieldOf("position_predicate", PosAlwaysTrueTest.INSTANCE).forGetter(OverhauledRule::positionPredicate),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("probability", 1.0F).forGetter(OverhauledRule::probability),
                    Codec.BOOL.optionalFieldOf("copy_properties", false).forGetter(OverhauledRule::copyProperties),
                    RuleOutput.CODEC.fieldOf("output").forGetter(OverhauledRule::output)
            ).apply(instance, OverhauledRule::new)
    );

    /**
     * Evaluates whether the input state, location state, and block positions satisfy this rule's predicates.
     *
     * @param inputState    The original block state from the template.
     * @param locationState The current block state at the target placement position.
     * @param localPos      The unrotated template-local block position.
     * @param absolutePos   The transformed world-space block position.
     * @param pivot         The structure pivot position.
     * @param random        The per-block deterministic random source.
     * @return {@code true} if at least one input predicate, at least one location predicate, and the position predicate all match; {@code false} otherwise.
     */
    public boolean test(BlockState inputState, BlockState locationState, BlockPos localPos, BlockPos absolutePos, BlockPos pivot, RandomSource random) {
        boolean inputMatches = this.inputPredicates.stream().anyMatch(predicate -> predicate.test(inputState, random));
        boolean locationMatches = this.locationPredicates.stream().anyMatch(predicate -> predicate.test(locationState, random));

        return inputMatches
                && locationMatches
                && this.positionPredicate.test(localPos, absolutePos, pivot, random);
    }
}