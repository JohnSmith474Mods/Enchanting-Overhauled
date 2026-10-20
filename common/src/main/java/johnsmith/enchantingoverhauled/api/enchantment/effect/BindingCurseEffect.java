package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * A data-driven record defining the parameters for the Binding Curse enchantment effect component.
 * <p>
 * This effect controls the <b>probabilistic chance</b> that an armor item equipped with the
 * Curse of Binding will be saved and restored to its original armor slot upon the player's death,
 * expanding upon the vanilla behavior where the item only prevents manual removal.
 *
 * @param chance A {@link LevelBasedValue} that calculates the probability ({@code 0.0} to {@code 1.0})
 *               that the bound armor item will be saved and kept when the player dies.
 * @see EnchantmentEffectComponents#BINDING_CHANCE
 */
public record BindingCurseEffect(LevelBasedValue chance) {

    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     * Maps the {@code "chance"} field to a {@link LevelBasedValue}.
     */
    public static final Codec<BindingCurseEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("chance").forGetter(BindingCurseEffect::chance)
    ).apply(instance, BindingCurseEffect::new));
}