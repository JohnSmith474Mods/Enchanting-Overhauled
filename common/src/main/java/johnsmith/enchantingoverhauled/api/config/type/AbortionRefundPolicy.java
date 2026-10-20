package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * An enumeration specifying when an enchantment source catalyst should be consumed
 * or processed during the Scribe minigame, dictating whether it can be refunded
 * if the player aborts the game before completion.
 */
public enum AbortionRefundPolicy implements StringRepresentable {
    /**
     * All non-persistent sources are processed immediately upon starting the Scribe minigame.
     * Aborting the game will not refund the consumed enchantment.
     */
    NONE("none"),

    /**
     * Only fleeting sources (tagged with {@link johnsmith.enchantingoverhauled.api.tag.Tags#FLEETING_ENCHANTMENT_SOURCE})
     * are processed immediately on start. Untagged sources wait until the game is won or lost.
     */
    UNTAGGED("untagged"),

    /**
     * No sources are processed immediately on start. All non-persistent sources wait until
     * the minigame concludes in a win or loss, allowing full refunds on abort.
     */
    FULL("full");

    /**
     * The serialization codec for encoding and decoding {@link AbortionRefundPolicy} values by name.
     */
    public static final Codec<AbortionRefundPolicy> CODEC = StringRepresentable.fromEnum(AbortionRefundPolicy::values);

    /**
     * The serialized string identifier for this policy.
     */
    private final String id;

    /**
     * Constructs an {@code AbortionRefundPolicy} enum constant.
     *
     * @param id The serialized string identifier.
     */
    AbortionRefundPolicy(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this refund policy.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}