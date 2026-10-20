package johnsmith.enchantingoverhauled.api.enchantment.spellfield;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the tactical combat role of an {@link AiSpellField}, its default {@link SpellTarget},
 * and its default {@link CastType}.
 */
public enum SpellRole implements StringRepresentable {

    /**
     * Offensive role aimed at damaging,debuffing, or displacing hostile targets
     * (defaults to {@link SpellTarget#HOSTILE} and {@link CastType#PROJECTILE}).
     */
    OFFENSIVE("offensive", SpellTarget.HOSTILE, CastType.PROJECTILE),

    /**
     * Defensive role aimed at warding, shielding, or creating decoys for the caster
     * (defaults to {@link SpellTarget#SELF} and {@link CastType#DIRECT}).
     */
    DEFENSIVE("defensive", SpellTarget.SELF, CastType.DIRECT),

    /**
     * Supportive role aimed at healing or buffing allies or hindering enemies
     * (defaults to {@link SpellTarget#ALLY} and {@link CastType#DIRECT}).
     */
    SUPPORTIVE("supportive", SpellTarget.ALLY, CastType.DIRECT);

    /**
     * The serialization codec for encoding and decoding {@link SpellRole} values by name.
     */
    public static final Codec<SpellRole> CODEC = StringRepresentable.fromEnum(SpellRole::values);

    /**
     * The serialized string identifier for this spell role.
     */
    private final String id;

    /**
     * The default {@link SpellTarget} used when a spell definition omits an explicit target.
     */
    private final SpellTarget defaultTarget;

    /**
     * The default {@link CastType} used when a spell definition omits an explicit cast type.
     */
    private final CastType defaultCastType;

    /**
     * Constructs a {@code SpellRole} enum constant.
     *
     * @param id              The serialized string identifier.
     * @param defaultTarget   The default {@link SpellTarget} for this role.
     * @param defaultCastType The default {@link CastType} for this role.
     */
    SpellRole(String id, SpellTarget defaultTarget, CastType defaultCastType) {
        this.id = id;
        this.defaultTarget = defaultTarget;
        this.defaultCastType = defaultCastType;
    }

    /**
     * Retrieves the default {@link SpellTarget} associated with this tactical role.
     *
     * @return The default {@link SpellTarget}.
     */
    public SpellTarget getDefaultTarget() {
        return this.defaultTarget;
    }

    /**
     * Retrieves the default {@link CastType} associated with this tactical role.
     *
     * @return The default {@link CastType}.
     */
    public CastType getDefaultCastType() {
        return this.defaultCastType;
    }

    /**
     * Retrieves the serialized string representation of this spell role.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}