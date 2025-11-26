package johnsmith.enchantingoverhauled.api.enchantment.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

public record DiminishingReturnsValue(float base, float decrement, float minimum) implements LevelBasedValue {

    public static final MapCodec<DiminishingReturnsValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("base").forGetter(DiminishingReturnsValue::base),
            Codec.FLOAT.fieldOf("decrement").forGetter(DiminishingReturnsValue::decrement),
            Codec.FLOAT.fieldOf("minimum").forGetter(DiminishingReturnsValue::minimum)
    ).apply(instance, DiminishingReturnsValue::new));

    @Override
    public float calculate(int level) {
        if (level <= 0) return 0;

        // Edge Case: Non-diminishing or increasing values (fallback to loop or constant)
        if (this.decrement <= 0.0001f) {
            if (this.decrement == 0) {
                return level * Math.max(this.base, this.minimum);
            }
            // Fallback for negative decrement (increasing returns) to ensure correct clamping behavior
            return calculateIterative(level);
        }

        // 1. Determine how many terms exist before we hit the minimum floor.
        float limit = (this.base - this.minimum) / this.decrement;
        int termCount = Math.max(1, (int) Math.floor(limit) + 1);

        // 2. Determine how many levels actually fall into the arithmetic progression phase vs the constant phase.
        int count = Math.min(level, termCount);

        // 3. Calculate sum of arithmetic progression: S_n = n/2 * (2a + (n-1)d)
        // a = base, d = -decrement
        float sum = count * (2 * this.base - (count - 1) * this.decrement) / 2f;

        // 4. Add the constant tail if the level exceeds the arithmetic progression phase
        if (level > count) {
            int remaining = level - count;
            sum += remaining * this.minimum;
        }

        return sum;
    }

    private float calculateIterative(int level) {
        float currentBase = this.base;
        float total = 0;
        for (int i = 0; i < level; i++) {
            total += currentBase;
            currentBase = Math.max(currentBase - this.decrement, this.minimum);
        }
        return total;
    }

    @Override
    public @NotNull MapCodec<? extends LevelBasedValue> codec() {
        return CODEC;
    }
}