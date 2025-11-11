package johnsmith.api.enchantment.theme.power.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A power bonus that adds a fixed amount if a block state property
 * matches a specific string value.
 *
 * @param property The name of the property (e.g., "lit").
 * @param value The string value to check for (e.g., "true").
 * @param bonus The flat amount of power to add if the check passes.
 */
public record AddIfBlockStateBonus(
        String property,
        String value,
        int bonus
) implements PowerBonus {

    public static final MapCodec<AddIfBlockStateBonus> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("property").forGetter(AddIfBlockStateBonus::property),
                    Codec.STRING.fieldOf("value").forGetter(AddIfBlockStateBonus::value),
                    Codec.INT.fieldOf("bonus").forGetter(AddIfBlockStateBonus::bonus)
            ).apply(instance, AddIfBlockStateBonus::new)
    );

    @Override
    public String getTypeId() {
        return PowerBonusType.ADD_IF_BLOCK_STATE.getId();
    }

    @Override
    public MapCodec<? extends PowerBonus> getCodec() {
        return CODEC;
    }
}