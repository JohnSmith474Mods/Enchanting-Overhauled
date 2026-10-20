package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * An enumeration specifying the resolution protocol invoked when the Enchanting Altar
 * detects a conflict where a supplied source catalyst possesses a strictly greater
 * enchantment level than the item target.
 * <p>
 * This defines whether the altar fundamentally acts to overwrite existing states directly
 * or relies on probabilistic enhancement mechanics.
 */
public enum UpgradePriorityType implements StringRepresentable {
    /**
     * Resolves the conflict by overwriting the target's current enchantment with the
     * exact level found on the source catalyst. Strictly deterministic.
     */
    TRANSFER("transfer"),

    /**
     * Resolves the conflict by consuming the source as a catalyst to unlock the standard
     * probabilistic upgrade roll mechanism from the current level.
     */
    UPGRADE("upgrade");

    /**
     * The serialization codec linking this enum to the configuration/data system.
     */
    public static final Codec<UpgradePriorityType> CODEC = StringRepresentable.fromEnum(UpgradePriorityType::values);

    /**
     * The internal string ID used for serialization.
     */
    private final String id;

    /**
     * Constructs the priority type.
     *
     * @param id The internal string ID.
     */
    UpgradePriorityType(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this priority type.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}