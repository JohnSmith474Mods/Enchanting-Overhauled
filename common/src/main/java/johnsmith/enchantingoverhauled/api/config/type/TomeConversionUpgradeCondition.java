package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the enchantment level condition under which an Enchanted Book on a Lectern
 * converts into an Enchanted Tome when upgrading an existing enchantment via the Scribe minigame.
 */
public enum TomeConversionUpgradeCondition implements StringRepresentable {
    /**
     * Always converts the Enchanted Book into an Enchanted Tome upon any successful upgrade,
     * regardless of the resulting enchantment level.
     */
    NONE("none"),

    /**
     * Converts the Enchanted Book into an Enchanted Tome when the upgraded enchantment level
     * reaches or exceeds a user-configured custom threshold.
     */
    CUSTOM("custom"),

    /**
     * Converts the Enchanted Book into an Enchanted Tome when the upgraded enchantment level
     * reaches or exceeds its natural maximum level.
     */
    MAXED("maxed"),

    /**
     * Converts the Enchanted Book into an Enchanted Tome only when the upgraded enchantment level
     * strictly exceeds its natural maximum level (overleveled).
     */
    OVERLEVELED("overleveled"),

    /**
     * Never converts an Enchanted Book into an Enchanted Tome when upgrading an enchantment.
     */
    DISABLED("disabled");

    /**
     * The serialization codec for encoding and decoding {@link TomeConversionUpgradeCondition} values by name.
     */
    public static final Codec<TomeConversionUpgradeCondition> CODEC = StringRepresentable.fromEnum(TomeConversionUpgradeCondition::values);

    /**
     * The serialized string identifier for this upgrade conversion condition.
     */
    private final String id;

    /**
     * Constructs a {@code TomeConversionUpgradeCondition} enum constant.
     *
     * @param id The serialized string identifier.
     */
    TomeConversionUpgradeCondition(String id) {
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