package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * An enumeration specifying which algorithmic variant of the Scribe Minesweeper minigame is active.
 */
public enum ScribeVariant implements StringRepresentable {
    /**
     * The standard Minesweeper variant where all mine positions are deterministically placed
     * immediately after the player's first revealed cell.
     */
    CLASSIC("classic"),

    /**
     * A quantum constraint-solving variant that dynamically resolves mine placements on each reveal
     * to forgive unavoidable 50/50 guesses while punishing unforced guesses.
     */
    SUPERPOSITION("superposition");

    /**
     * The serialization codec for encoding and decoding {@link ScribeVariant} values by name.
     */
    public static final Codec<ScribeVariant> CODEC = StringRepresentable.fromEnum(ScribeVariant::values);

    /**
     * The serialized string identifier for this minigame variant.
     */
    private final String id;

    /**
     * Constructs a {@code ScribeVariant} enum constant.
     *
     * @param id The serialized string identifier.
     */
    ScribeVariant(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this Scribe variant.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}