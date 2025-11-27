package johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A data-driven power bonus implementation that adds a fixed {@code bonus} amount
 * to the enchanting power if a specific block state property on the power-providing block
 * matches a designated string {@code value}.
 * <p>
 * This is useful for conditional power, such as granting a bonus only if a candle is lit.
 *
 * @param property The name of the {@link net.minecraft.world.level.block.state.properties.Property}
 * to check on the block state (e.g., "lit" or "has_book").
 * @param value    The expected string representation of the property's value for the bonus to apply (e.g., "true").
 * @param bonus    The flat integer amount of enchanting power to add if the condition is met.
 */
public record AddIfBlockStateBonus(
        String property,
        String value,
        int bonus
) implements PowerBonus {
    /**
     * The codec responsible for serializing and deserializing instances of this record from data files (e.g., JSON).
     */
    public static final MapCodec<AddIfBlockStateBonus> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("property").forGetter(AddIfBlockStateBonus::property),
                    Codec.STRING.fieldOf("value").forGetter(AddIfBlockStateBonus::value),
                    Codec.INT.fieldOf("bonus").forGetter(AddIfBlockStateBonus::bonus)
            ).apply(instance, AddIfBlockStateBonus::new)
    );

    /**
     * {@inheritDoc}
     *
     * @return The constant ID string for this bonus type: "add_if_block_state".
     */
    @Override
    public String getTypeId() {
        return PowerBonusType.ADD_IF_BLOCK_STATE.getId();
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@link MapCodec} for this bonus type.
     */
    @Override
    public MapCodec<? extends PowerBonus> getCodec() {
        return CODEC;
    }
}