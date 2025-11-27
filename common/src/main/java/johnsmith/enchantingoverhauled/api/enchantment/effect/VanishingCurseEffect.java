package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * A data-driven record defining the parameters for the Vanishing Curse enchantment component.
 * <p>
 * This effect introduces a **probabilistic chance** that an item, when cursed with
 * Vanishing, will survive and be retained by the player upon death, overriding the
 * vanilla behavior where the item is guaranteed to be destroyed.
 *
 * @param chance A {@link LevelBasedValue} that calculates the probability (0.0 to 1.0)
 * that the cursed item will **survive** and be retained when the player dies.
 */
public record VanishingCurseEffect(LevelBasedValue chance) {
    /**
     * The codec responsible for serializing and deserializing instances of this record from data files (e.g., JSON).
     * It maps the single {@code "chance"} field to a {@link LevelBasedValue}.
     */
    public static final Codec<VanishingCurseEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("chance").forGetter(VanishingCurseEffect::chance)
    ).apply(instance, VanishingCurseEffect::new));
}