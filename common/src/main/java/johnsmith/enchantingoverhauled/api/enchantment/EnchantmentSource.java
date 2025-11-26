package johnsmith.enchantingoverhauled.api.enchantment;

import net.minecraft.util.ByIdMap;

import java.util.Arrays;
import java.util.function.IntFunction;

public enum EnchantmentSource {
    NONE(-1),
    TARGET(0),
    SOURCE(1),
    TABLE(2);

    private final int id;

    EnchantmentSource(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    // Efficient lookup function (Vanilla style)
    // Note: We must exclude NONE (-1) because continuous map requires non-negative, sequential IDs starting at 0.
    private static final IntFunction<EnchantmentSource> BY_ID = ByIdMap.continuous(
            EnchantmentSource::getId,
            Arrays.stream(values()).filter(v -> v.id >= 0).toArray(EnchantmentSource[]::new),
            ByIdMap.OutOfBoundsStrategy.WRAP
    );

    public static EnchantmentSource byId(int id) {
        if (id == -1) return NONE;
        return BY_ID.apply(id);
    }
}