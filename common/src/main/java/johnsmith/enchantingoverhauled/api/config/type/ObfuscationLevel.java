package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the strictness level at which enchantment sources and table offerings
 * are obfuscated using the Standard Galactic Alphabet ({@code minecraft:alt} font)
 * in tooltips and GUI widgets.
 */
public enum ObfuscationLevel implements StringRepresentable {
    /**
     * Never obfuscates enchantment names or descriptions.
     */
    NONE("none"),

    /**
     * Obfuscates only fleeting enchantment sources (items tagged with
     * {@link johnsmith.enchantingoverhauled.api.tag.Tags#FLEETING_ENCHANTMENT_SOURCE}).
     */
    FLEETING("fleeting"),

    /**
     * Obfuscates fleeting enchantment sources, untagged stored enchantment sources,
     * and unapplied table offerings, leaving persistent sources (such as Enchanted Tomes) legible.
     */
    UNTAGGED("untagged"),

    /**
     * Obfuscates all stored enchantment sources and table offerings regardless of tags.
     */
    FULL("full");

    /**
     * The serialization codec for encoding and decoding {@link ObfuscationLevel} values by name.
     */
    public static final Codec<ObfuscationLevel> CODEC = StringRepresentable.fromEnum(ObfuscationLevel::values);

    /**
     * The serialized string identifier for this obfuscation level.
     */
    private final String id;

    /**
     * Constructs an {@code ObfuscationLevel} enum constant.
     *
     * @param id The serialized string identifier.
     */
    ObfuscationLevel(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this obfuscation level.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}