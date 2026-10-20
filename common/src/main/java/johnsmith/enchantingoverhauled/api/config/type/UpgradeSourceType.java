package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * An enumeration specifying the minimum relational level constraint that a source
 * catalyst must satisfy to be deemed valid for executing an enchantment upgrade.
 * <p>
 * Controls how punishing or flexible the mod's core progression loop is.
 */
public enum UpgradeSourceType implements StringRepresentable {
    // region Constants
    /**
     * The most permissive option. Validates pure upgrades without requiring
     * any source catalyst to be present in the altar, bypassing conflict resolution entirely.
     */
    NONE("none"),

    /**
     * The moderate option. The catalyst must supply the exact same enchantment at a level
     * equal to or strictly greater than the target's current level.
     */
    EQUAL("equal"),

    /**
     * The strictest option. The catalyst must supply the exact same enchantment at a level
     * strictly greater than the target's current level to permit an upgrade.
     */
    GREATER("greater");
    // endregion

    // region Properties
    /**
     * The serialization codec linking this enum to the configuration/data system.
     */
    public static final Codec<UpgradeSourceType> CODEC = StringRepresentable.fromEnum(UpgradeSourceType::values);

    /**
     * The internal string ID used for serialization.
     */
    private final String id;
    // endregion

    // region Initialization
    /**
     * Constructs the source requirement type.
     *
     * @param id The internal string ID.
     */
    UpgradeSourceType(String id) {
        this.id = id;
    }
    // endregion

    // region Overrides
    /**
     * Retrieves the serialized string representation of this upgrade source type.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
    // endregion
}