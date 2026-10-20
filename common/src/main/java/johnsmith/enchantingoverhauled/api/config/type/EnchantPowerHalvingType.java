package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Defines how the available enchanting power at an Enchanting Altar is halved
 * across multiple generated enchantment slots.
 */
public enum EnchantPowerHalvingType implements StringRepresentable {
    /**
     * Available enchanting power is never halved across slots; every generated slot
     * utilizes the full available enchanting power.
     */
    NONE("none"),

    /**
     * Available enchanting power is halved after each newly rolled table enchantment,
     * unaffected by existing target or source slots.
     */
    PER_TABLE_ROLL("per_table_roll"),

    /**
     * Available enchanting power is halved for each occupied context slot prior to rolling
     * and halved again after each subsequent table slot roll.
     */
    PER_SLOT("per_slot");

    /**
     * The serialization codec linking this enum to the configuration and data system.
     */
    public static final Codec<EnchantPowerHalvingType> CODEC = StringRepresentable.fromEnum(EnchantPowerHalvingType::values);

    /**
     * The internal string ID used for serialization.
     */
    private final String id;

    /**
     * Constructs the halving type.
     *
     * @param id The internal string ID.
     */
    EnchantPowerHalvingType(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this halving type.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}