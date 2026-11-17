package johnsmith.enchantingoverhauled.api.enchantment.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.enchantment.LevelBasedValue;

import org.jetbrains.annotations.NotNull;

/**
 * A specialized implementation of {@link LevelBasedValue} that calculates the success
 * or failure of a probabilistic event based on an inner {@code chance} provider.
 * <p>
 * This value is intended for use with game mechanics that require a boolean outcome (success/fail)
 * where 0.0 represents a success/false outcome, and 1.0 represents a failure/true outcome.
 * <p>
 * **Intended Usage Example (Minecraft's {@code minecraft:ammo_use}):**
 * <ul>
 * <li>If {@code calculate} returns 0.0: Ammo is saved (Infinite Ammo/Success).</li>
 * <li>If {@code calculate} returns 1.0: Ammo is consumed (Failure).</li>
 * </ul>
 *
 * @param chance The inner {@link LevelBasedValue} that calculates the probability (0.0 to 1.0)
 * that the intended positive outcome (e.g., saving ammo, avoiding curse destruction) occurs.
 */
public record ProbabilisticValue(LevelBasedValue chance) implements LevelBasedValue {

    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     * It maps the nested {@code "chance"} field to a generic {@link LevelBasedValue}.
     */
    public static final MapCodec<ProbabilisticValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("chance").forGetter(ProbabilisticValue::chance)
    ).apply(instance, ProbabilisticValue::new));

    /**
     * Calculates a resulting float value of 0.0 or 1.0 based on a random roll against the {@code saveChance}.
     *
     * @param level The level of the enchantment.
     * @return 0.0F if a random number is less than {@code saveChance} (Success/False).
     * 1.0F otherwise (Failure/True).
     */
    @Override
    public float calculate(int level) {
        float saveChance = this.chance.calculate(level);

        if (Math.random() < saveChance) {
            return 0.0F;
        }
        return 1.0F;
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@link MapCodec} for this class.
     */
    @Override
    public @NotNull MapCodec<? extends LevelBasedValue> codec() {
        return CODEC;
    }
}