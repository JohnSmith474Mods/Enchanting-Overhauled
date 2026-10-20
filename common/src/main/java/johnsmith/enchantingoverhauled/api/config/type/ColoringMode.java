package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Defines which categories of enchantments receive custom thematic or level-based coloring
 * in tooltips and GUI widgets.
 */
public enum ColoringMode implements StringRepresentable {
    /**
     * Disables custom coloring, preserving default vanilla text colors.
     */
    NONE("none"),

    /**
     * Applies custom coloring only to standard (non-curse) enchantments, keeping curses red.
     */
    ENCHANTMENT("enchantment"),

    /**
     * Applies custom coloring only to curse enchantments.
     */
    CURSE("curse"),

    /**
     * Applies custom coloring to all enchantments, including curses.
     */
    ALL("ALL");

    /**
     * The serialization codec for encoding and decoding {@link ColoringMode} values by name.
     */
    public static final Codec<ColoringMode> CODEC = StringRepresentable.fromEnum(ColoringMode::values);

    /**
     * The serialized string identifier for this coloring mode.
     */
    private final String id;

    /**
     * Constructs a {@code ColoringMode} enum constant.
     *
     * @param id The serialized string identifier.
     */
    ColoringMode(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this coloring mode.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}