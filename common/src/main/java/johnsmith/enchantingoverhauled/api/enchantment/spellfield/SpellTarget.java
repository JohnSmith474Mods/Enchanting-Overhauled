package johnsmith.enchantingoverhauled.api.enchantment.spellfield;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the intended target entity category for an {@link AiSpellField}.
 */
public enum SpellTarget implements StringRepresentable {

    /**
     * The spell targets the casting entity itself.
     */
    SELF("self"),

    /**
     * The spell targets a nearby allied entity (tagged with {@link johnsmith.enchantingoverhauled.api.tag.Tags#ENCHANTED_TOME_ALLIES}).
     */
    ALLY("ally"),

    /**
     * The spell targets the caster's current hostile combat target.
     */
    HOSTILE("hostile");

    /**
     * The serialization codec for encoding and decoding {@link SpellTarget} values by name.
     */
    public static final Codec<SpellTarget> CODEC = StringRepresentable.fromEnum(SpellTarget::values);

    /**
     * The serialized string identifier for this target category.
     */
    private final String id;

    /**
     * Constructs a {@code SpellTarget} enum constant.
     *
     * @param id The serialized string identifier.
     */
    SpellTarget(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this spell target category.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}