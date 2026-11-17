package johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A data-driven power bonus implementation that multiplies the base enchanting power
 * by the integer value of a specified block state property.
 * <p>
 * This is designed to model variable power sources, such as multiplying the base power
 * provided by a candle by the number of candles in a group, using the 'candles' block state property.
 *
 * @param property The name of the {@link net.minecraft.world.level.block.state.properties.Property}
 * to check on the block state (e.g., "candles"). This property must hold an integer value.
 */
public record MultiplyByBlockStateBonus(
        String property
) implements PowerBonus {
    /**
     * The codec responsible for serializing and deserializing instances of this record from data files (e.g., JSON).
     */
    public static final MapCodec<MultiplyByBlockStateBonus> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("property").forGetter(MultiplyByBlockStateBonus::property)
            ).apply(instance, MultiplyByBlockStateBonus::new)
    );

    /**
     * {@inheritDoc}
     *
     * @return The constant ID string for this bonus type: "multiply_by_block_state".
     */
    @Override
    public String getTypeId() {
        return PowerBonusType.MULTIPLY_BY_BLOCK_STATE.getId();
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