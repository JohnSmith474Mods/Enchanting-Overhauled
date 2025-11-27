package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * A data-driven record defining the parameters for the Binding Curse enchantment component.
 * <p>
 * This effect controls the **probabilistic chance** that an armor item, when equipped
 * with the Binding Curse, will be saved and restored upon the player's death, overriding
 * the vanilla behavior where the item is guaranteed to be kept and prevent armor changes.
 *
 * @param chance A {@link LevelBasedValue} that calculates the probability (0.0 to 1.0)
 * that the item will be saved/kept when the player dies.
 */
public record BindingCurseEffect(LevelBasedValue chance) {
    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     * It maps the {@code "chance"} field to a {@link LevelBasedValue}.
     */
    public static final Codec<BindingCurseEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("chance").forGetter(BindingCurseEffect::chance)
    ).apply(instance, BindingCurseEffect::new));
}