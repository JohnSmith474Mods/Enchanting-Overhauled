package johnsmith.enchantingoverhauled.api.enchantment.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

/**
 * Returns 0.0 (Consumed = False) or 1.0 (Consumed = True) based on a probability.
 * <p>
 * Usage in "minecraft:ammo_use":
 * - Result 0.0 -> Infinite Ammo (Success)
 * - Result 1.0 -> Consume Ammo (Failure)
 */
public record ProbabilisticValue(LevelBasedValue chance) implements LevelBasedValue {

    public static final MapCodec<ProbabilisticValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("chance").forGetter(ProbabilisticValue::chance)
    ).apply(instance, ProbabilisticValue::new));

    @Override
    public float calculate(int level) {
        // Calculate the chance to SAVE the arrow (e.g., 0.33 for 33%)
        float saveChance = this.chance.calculate(level);

        // We use Math.random() here because LevelBasedValue context is stateless.
        // This is safe for server-side logic like arrow consumption.
        if (Math.random() < saveChance) {
            return 0.0F; // Success: Set consumption to 0
        }
        return 1.0F; // Failure: Set consumption to 1
    }

    @Override
    public @NotNull MapCodec<? extends LevelBasedValue> codec() {
        return CODEC;
    }
}