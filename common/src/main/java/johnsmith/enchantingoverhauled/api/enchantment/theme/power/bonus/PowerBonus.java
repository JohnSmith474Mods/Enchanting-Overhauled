package johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/**
 * A sealed interface representing a conditional bonus applied to the base enchanting power
 * provided by a single block in an {@link johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme}.
 * <p>
 * Implementations of this interface define dynamic rules (such as checking block states or counting
 * container inventory items) that modify the final power value.
 *
 * @see AddIfBlockStateBonus
 * @see CountItemsInInventoryBonus
 * @see MultiplyByBlockStateBonus
 */
public sealed interface PowerBonus permits
        AddIfBlockStateBonus,
        CountItemsInInventoryBonus,
        MultiplyByBlockStateBonus {

    /**
     * The primary codec for serializing and deserializing any implementation of {@code PowerBonus}.
     * <p>
     * Uses a type dispatch mechanism based on the string value of the {@code "type"} field in JSON,
     * delegating decoding to the appropriate codec retrieved via {@link PowerBonusType#getCodecById(String)}.
     */
    Codec<PowerBonus> CODEC = Codec.STRING.dispatch(
            "type",
            PowerBonus::getTypeId,
            PowerBonusType::getCodecById
    );

    /**
     * Retrieves the unique identifier string associated with this specific bonus implementation.
     *
     * @return The unique ID string (e.g., {@code "add_if_block_state"}).
     */
    String getTypeId();

    /**
     * Retrieves the {@link MapCodec} responsible for encoding and decoding this specific bonus implementation.
     *
     * @return The specialized {@link MapCodec} for this class.
     */
    MapCodec<? extends PowerBonus> getCodec();
}