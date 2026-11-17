package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

/**
 * A data-driven record defining the parameters for the Silk Touch enchantment component.
 * <p>
 * This effect is used to introduce a **probabilistic chance** for the Silk Touch enchantment
 * to succeed, overriding the default vanilla 100% success rate. The outcome determines
 * whether a mined block drops itself or drops its normal fortune/tool-appropriate loot.
 *
 * @param chance A {@link LevelBasedValue} that calculates the probability (0.0 to 1.0)
 * that the Silk Touch effect will activate successfully at a specific enchantment level.
 */
public record SilkTouchEffect(LevelBasedValue chance) {

    /**
     * The codec responsible for serializing and deserializing instances of this record from data files (e.g., JSON).
     * It maps the single {@code "chance"} field to a {@link LevelBasedValue}.
     */
    public static final Codec<SilkTouchEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("chance").forGetter(SilkTouchEffect::chance)
    ).apply(instance, SilkTouchEffect::new));
}