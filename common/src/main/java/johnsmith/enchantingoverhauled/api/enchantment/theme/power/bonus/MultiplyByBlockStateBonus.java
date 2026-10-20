package johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A data-driven {@link PowerBonus} implementation that multiplies the base enchanting power
 * by the integer value of a specified block state property.
 * <p>
 * Designed to model variable power sources, such as multiplying the base power provided by a candle
 * by the number of candles in a cluster using the {@code "candles"} integer block state property.
 *
 * @param property The name of the {@link net.minecraft.world.level.block.state.properties.IntegerProperty}
 *                 to check on the block state (e.g., {@code "candles"}).
 */
public record MultiplyByBlockStateBonus(
        String property
) implements PowerBonus {

    /**
     * The codec responsible for serializing and deserializing instances of this record from JSON.
     */
    public static final MapCodec<MultiplyByBlockStateBonus> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("property").forGetter(MultiplyByBlockStateBonus::property)
            ).apply(instance, MultiplyByBlockStateBonus::new)
    );

    /**
     * {@inheritDoc}
     *
     * @return The constant ID string for this bonus type: {@code "multiply_by_block_state"}.
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