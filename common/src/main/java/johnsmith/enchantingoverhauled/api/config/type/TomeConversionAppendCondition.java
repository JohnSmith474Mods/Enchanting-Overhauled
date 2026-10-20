package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the condition under which an Enchanted Book on a Lectern converts into an
 * Enchanted Tome when a new enchantment is appended to it via the Scribe minigame.
 */
public enum TomeConversionAppendCondition implements StringRepresentable {
    /**
     * Converts the Enchanted Book into an Enchanted Tome when the total number of stored
     * enchantments reaches or exceeds the user-configured custom threshold.
     */
    CUSTOM("custom"),

    /**
     * Converts the Enchanted Book into an Enchanted Tome when the total number of stored
     * enchantments reaches the global maximum enchantment capacity.
     */
    CAP("cap"),

    /**
     * Never converts an Enchanted Book into an Enchanted Tome when appending a new enchantment.
     */
    DISABLED("disabled");

    /**
     * The serialization codec for encoding and decoding {@link TomeConversionAppendCondition} values by name.
     */
    public static final Codec<TomeConversionAppendCondition> CODEC = StringRepresentable.fromEnum(TomeConversionAppendCondition::values);

    /**
     * The serialized string identifier for this append conversion condition.
     */
    private final String id;

    /**
     * Constructs a {@code TomeConversionAppendCondition} enum constant.
     *
     * @param id The serialized string identifier.
     */
    TomeConversionAppendCondition(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this conversion condition.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}