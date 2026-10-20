package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Defines on which enchantment component types curse enchantments (tagged with
 * {@link net.minecraft.tags.EnchantmentTags#CURSE}) should be hidden from item tooltips.
 */
public enum CurseConcealmentMode implements StringRepresentable {
    /**
     * Curses are never concealed and always appear on item tooltips.
     */
    NONE("none"),

    /**
     * Curses are concealed only when applied directly to equipment ({@code minecraft:enchantments}),
     * remaining visible on storage items such as books, pages, and tomes.
     */
    APPLIED("applied"),

    /**
     * Curses are concealed only when stored on enchantment catalysts ({@code minecraft:stored_enchantments}).
     */
    STORED("stored"),

    /**
     * Curses are concealed on both applied equipment and stored enchantment catalysts.
     */
    ALL("all");

    /**
     * The serialization codec for encoding and decoding {@link CurseConcealmentMode} values by name.
     */
    public static final Codec<CurseConcealmentMode> CODEC = StringRepresentable.fromEnum(CurseConcealmentMode::values);

    /**
     * The serialized string identifier for this concealment mode.
     */
    private final String id;

    /**
     * Constructs a {@code CurseConcealmentMode} enum constant.
     *
     * @param id The serialized string identifier.
     */
    CurseConcealmentMode(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this curse concealment mode.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}