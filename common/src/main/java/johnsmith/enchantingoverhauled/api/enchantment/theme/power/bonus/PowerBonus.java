package johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/**
 * A sealed interface representing a conditional power bonus.
 * Uses a dispatch codec to select the correct implementation based on the "type" field in JSON.
 */
public sealed interface PowerBonus permits
        AddIfBlockStateBonus,
        CountItemsInInventoryBonus,
        MultiplyByBlockStateBonus
{
    Codec<PowerBonus> CODEC = Codec.STRING.dispatch(
            "type",
            PowerBonus::getTypeId,
            PowerBonusType::getCodecById
    );

    String getTypeId();

    MapCodec<? extends PowerBonus> getCodec();
}