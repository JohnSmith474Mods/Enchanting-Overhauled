package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * A data-driven record defining the parameters for the Silk Touch enchantment effect component.
 * <p>
 * This effect introduces a <b>probabilistic chance</b> for the Silk Touch enchantment to succeed
 * based on its enchantment level, overriding the default vanilla 100% success rate. The outcome
 * determines whether a mined block drops itself or drops its normal loot.
 *
 * @param chance A {@link LevelBasedValue} that calculates the probability ({@code 0.0} to {@code 1.0})
 *               that the Silk Touch effect will activate successfully at a specific enchantment level.
 * @see EnchantmentEffectComponents#SILK_TOUCH
 */
public record SilkTouchEffect(LevelBasedValue chance) {

    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     * Maps the single {@code "chance"} field to a {@link LevelBasedValue}.
     */
    public static final Codec<SilkTouchEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("chance").forGetter(SilkTouchEffect::chance)
    ).apply(instance, SilkTouchEffect::new));
}