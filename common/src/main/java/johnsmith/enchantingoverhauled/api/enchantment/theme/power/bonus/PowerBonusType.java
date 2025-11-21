package johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus;

import com.mojang.serialization.MapCodec;

public enum PowerBonusType {
    ADD_IF_BLOCK_STATE("add_if_block_state", AddIfBlockStateBonus.CODEC),
    COUNT_ITEMS_IN_INVENTORY("count_items_in_inventory", CountItemsInInventoryBonus.CODEC),
    MULTIPLY_BY_BLOCK_STATE("multiply_by_block_state", MultiplyByBlockStateBonus.CODEC);

    private final String id;
    private final MapCodec<? extends PowerBonus> codec;

    PowerBonusType(String id, MapCodec<? extends PowerBonus> codec) {
        this.id = id;
        this.codec = codec;
    }

    public String getId() {
        return id;
    }

    public MapCodec<? extends PowerBonus> getCodec() {
        return codec;
    }

    public static MapCodec<? extends PowerBonus> getCodecById(String id) {
        for (PowerBonusType type : values()) {
            if (type.id.equals(id)) {
                return type.codec;
            }
        }
        throw new IllegalArgumentException("Unknown PowerBonus type: " + id);
    }
}