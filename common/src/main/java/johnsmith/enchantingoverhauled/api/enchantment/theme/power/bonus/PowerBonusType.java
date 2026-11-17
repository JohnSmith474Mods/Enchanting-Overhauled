package johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus;

import com.mojang.serialization.MapCodec;

/**
 * An enumeration representing the different types of conditional power bonuses available
 * in the enchantment theme system.
 * <p>
 * This enum serves as the key component in the {@link PowerBonus} dispatch codec,
 * mapping the string ID found in configuration files (e.g., JSON) to the corresponding
 * {@link MapCodec} responsible for deserializing that specific bonus implementation.
 */
public enum PowerBonusType {
    /**
     * Identifies the bonus that adds power based on a matching block state property string value.
     */
    ADD_IF_BLOCK_STATE("add_if_block_state", AddIfBlockStateBonus.CODEC),
    /**
     * Identifies the bonus that adds power based on counting specific items within a block entity's inventory.
     */
    COUNT_ITEMS_IN_INVENTORY("count_items_in_inventory", CountItemsInInventoryBonus.CODEC),
    /**
     * Identifies the bonus that multiplies the base power by the integer value of a block state property.
     */
    MULTIPLY_BY_BLOCK_STATE("multiply_by_block_state", MultiplyByBlockStateBonus.CODEC);

    /**
     * The unique string identifier used in data files to refer to this bonus type.
     */
    private final String id;

    /**
     * The specialized codec responsible for encoding and decoding the specific {@link PowerBonus} implementation.
     */
    private final MapCodec<? extends PowerBonus> codec;

    /**
     * Constructs a {@code PowerBonusType}.
     *
     * @param id    The unique string identifier.
     * @param codec The corresponding map codec for the bonus class.
     */
    PowerBonusType(String id, MapCodec<? extends PowerBonus> codec) {
        this.id = id;
        this.codec = codec;
    }

    /**
     * Retrieves the unique string identifier for this bonus type.
     *
     * @return The unique ID string.
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the map codec associated with this bonus type.
     *
     * @return The {@link MapCodec} for the corresponding {@link PowerBonus} implementation.
     */
    public MapCodec<? extends PowerBonus> getCodec() {
        return codec;
    }

    /**
     * Static helper method used by the {@link PowerBonus#CODEC} dispatch logic.
     * <p>
     * It maps a raw string identifier (read from the "type" field in a data file)
     * to the appropriate {@link MapCodec} for deserialization.
     *
     * @param id The string identifier of the bonus type.
     * @return The corresponding {@link MapCodec} for that ID.
     * @throws IllegalArgumentException if the provided ID does not match any registered bonus type.
     */
    public static MapCodec<? extends PowerBonus> getCodecById(String id) {
        for (PowerBonusType type : values()) {
            if (type.id.equals(id)) {
                return type.codec;
            }
        }
        throw new IllegalArgumentException("Unknown PowerBonus type: " + id);
    }
}