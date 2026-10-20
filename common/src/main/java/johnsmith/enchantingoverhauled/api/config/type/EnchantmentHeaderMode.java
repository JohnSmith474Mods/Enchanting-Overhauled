package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Determines which enchantment component types display a section header
 * (e.g., "Applied Enchantments:" or "Stored Enchantments:") above their tooltip entries.
 */
public enum EnchantmentHeaderMode implements StringRepresentable {
    /**
     * No enchantment section headers are displayed on item tooltips.
     */
    NONE("none"),

    /**
     * Displays the header only for enchantments applied directly to equipment
     * ({@code minecraft:enchantments}).
     */
    APPLIED("applied"),

    /**
     * Displays the header only for enchantments stored on catalyst items
     * ({@code minecraft:stored_enchantments}).
     */
    STORED("stored"),

    /**
     * Displays headers for both applied and stored enchantment lists.
     */
    ALL("all");

    /**
     * The serialization codec for encoding and decoding {@link EnchantmentHeaderMode} values by name.
     */
    public static final Codec<EnchantmentHeaderMode> CODEC = StringRepresentable.fromEnum(EnchantmentHeaderMode::values);

    /**
     * The serialized string identifier for this header mode.
     */
    private final String id;

    /**
     * Constructs an {@code EnchantmentHeaderMode} enum constant.
     *
     * @param id The serialized string identifier.
     */
    EnchantmentHeaderMode(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this enchantment header mode.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}