package johnsmith.enchantingoverhauled.loot.function;

import com.mojang.serialization.MapCodec;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import org.jetbrains.annotations.NotNull;

/**
 * Central registry for the custom {@link LootItemFunctionType}s introduced by Enchanting Overhauled.
 * <p>
 * Instantiates each {@link LootItemFunctionType} without eager registration.
 */
public enum LootItemFunctionTypes implements StringRepresentable {
    ENCHANT_RANDOMLY("enchant_randomly", EnchantRandomlyFunction.CODEC),
    ENCHANT_FLEETING_SOURCE("enchant_fleeting_source", EnchantFleetingSourceFunction.CODEC),
    ENCHANT_SOURCE("enchant_source", EnchantSourceFunction.CODEC),
    ENCHANT_PERSISTENT_SOURCE("enchant_persistent_source", EnchantPersistentSourceFunction.CODEC);

    private final String id;
    private final MapCodec<? extends LootItemFunction> codec;
    private final LootItemFunctionType<? extends LootItemFunction> type;

    <T extends LootItemFunction> LootItemFunctionTypes(String id, MapCodec<T> codec) {
        this.id = id;
        this.codec = codec;
        this.type = new LootItemFunctionType<>(codec);
    }

    @SuppressWarnings("unchecked")
    public <T extends LootItemFunction> LootItemFunctionType<T> type() {
        return (LootItemFunctionType<T>) this.type;
    }

    @SuppressWarnings("unchecked")
    public <T extends LootItemFunction> MapCodec<T> codec() {
        return (MapCodec<T>) this.codec;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}