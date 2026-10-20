package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * A data-driven record defining the parameters for the Vanishing Curse enchantment effect component.
 * <p>
 * This effect introduces a <b>probabilistic chance</b> that an item cursed with Vanishing will
 * vanish and be destroyed upon the player's death, overriding the vanilla behavior where the item
 * is guaranteed to be destroyed 100% of the time.
 *
 * @param chance A {@link LevelBasedValue} that calculates the probability ({@code 0.0} to {@code 1.0})
 *               that the cursed item will vanish when the player dies.
 * @see EnchantmentEffectComponents#VANISHING_CHANCE
 */
public record VanishingCurseEffect(LevelBasedValue chance) {

    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     * Maps the single {@code "chance"} field to a {@link LevelBasedValue}.
     */
    public static final Codec<VanishingCurseEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("chance").forGetter(VanishingCurseEffect::chance)
    ).apply(instance, VanishingCurseEffect::new));
}