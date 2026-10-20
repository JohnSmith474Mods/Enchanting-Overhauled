package johnsmith.enchantingoverhauled.api.enchantment.spellfield;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Defines how an {@link AiSpellField} is delivered to its target when cast by an entity.
 */
public enum CastType implements StringRepresentable {

    /**
     * Fires a {@link johnsmith.enchantingoverhauled.entity.projectile.MagicMissileEntity} projectile
     * that deploys the spell field upon impacting an entity or block.
     */
    PROJECTILE("projectile"),

    /**
     * Deploys the spell field directly at the target entity's position without firing a projectile.
     */
    DIRECT("direct");

    /**
     * The serialization codec for encoding and decoding {@link CastType} values by name.
     */
    public static final Codec<CastType> CODEC = StringRepresentable.fromEnum(CastType::values);

    /**
     * The serialized string identifier for this cast type.
     */
    private final String id;

    /**
     * Constructs a {@code CastType} enum constant.
     *
     * @param id The serialized string identifier.
     */
    CastType(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this cast type.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}