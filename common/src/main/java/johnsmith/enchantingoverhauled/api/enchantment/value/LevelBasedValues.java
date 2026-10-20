package johnsmith.enchantingoverhauled.api.enchantment.value;

import com.mojang.serialization.MapCodec;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import org.jetbrains.annotations.NotNull;

/**
 * Enumeration of custom {@link LevelBasedValue} codecs registered by Enchanting Overhauled
 * into {@link net.minecraft.core.registries.BuiltInRegistries#ENCHANTMENT_LEVEL_BASED_VALUE_TYPE}.
 */
public enum LevelBasedValues implements StringRepresentable {

    /**
     * Registers {@link DifficultyBasedValue#CODEC} under {@code enchanting_overhauled:difficulty}.
     */
    DIFFICULTY("difficulty", DifficultyBasedValue.CODEC);

    /**
     * The serialized registry path identifier.
     */
    private final String id;

    /**
     * The {@link MapCodec} for the custom {@link LevelBasedValue} implementation.
     */
    private final MapCodec<? extends LevelBasedValue> codec;

    /**
     * Constructs a {@code LevelBasedValues} enum constant.
     *
     * @param id    The serialized registry path identifier.
     * @param codec The {@link MapCodec} for the level-based value type.
     */
    LevelBasedValues(String id, MapCodec<? extends LevelBasedValue> codec) {
        this.id = id;
        this.codec = codec;
    }

    /**
     * Retrieves the {@link MapCodec} associated with this level-based value type.
     *
     * @return The {@link MapCodec} instance.
     */
    public MapCodec<? extends LevelBasedValue> codec() {
        return this.codec;
    }

    /**
     * Retrieves the serialized registry path name for this level-based value type.
     *
     * @return The string identifier.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}