package johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A power bonus that adds a fixed amount multiplied by the property
 * if a block state property matches a specific string value.
 *
 * @param property The name of the property (e.g., "candles").
 */
public record MultiplyByBlockStateBonus(
        String property
) implements PowerBonus {
    public static final MapCodec<MultiplyByBlockStateBonus> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("property").forGetter(MultiplyByBlockStateBonus::property)
            ).apply(instance, MultiplyByBlockStateBonus::new)
    );

    @Override
    public String getTypeId() {
        return PowerBonusType.MULTIPLY_BY_BLOCK_STATE.getId();
    }

    @Override
    public MapCodec<? extends PowerBonus> getCodec() {
        return CODEC;
    }
}