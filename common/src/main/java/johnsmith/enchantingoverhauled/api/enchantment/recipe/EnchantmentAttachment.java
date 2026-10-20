package johnsmith.enchantingoverhauled.api.enchantment.recipe;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Defines how an enchantment is attached to the resulting output item when an {@link EnchantRecipe}
 * transmutation occurs (e.g., in the Enchanting Altar, Anvil, Tome Bartering, or Random Loot Functions).
 */
public enum EnchantmentAttachment implements StringRepresentable {

    /**
     * The enchantment is written to the output item's {@link net.minecraft.core.component.DataComponents#STORED_ENCHANTMENTS}
     * component (used for catalyst items such as Enchanted Books, Enchanted Pages, and Enchanted Tomes).
     */
    STORED("stored"),

    /**
     * The enchantment is written directly to the output item's {@link net.minecraft.core.component.DataComponents#ENCHANTMENTS}
     * component (used for active equipment).
     */
    APPLIED("applied"),

    /**
     * The enchantment is consumed by the transmutation process and discarded, leaving the output item
     * without enchantment components (used for transmutations like Golden Apple to Enchanted Golden Apple
     * or Splash Potion to Bottle o' Enchanting).
     */
    VOID("void");

    /**
     * The serialization codec for encoding and decoding {@link EnchantmentAttachment} values by name.
     */
    public static final Codec<EnchantmentAttachment> CODEC = StringRepresentable.fromEnum(EnchantmentAttachment::values);

    /**
     * The serialized string identifier for this attachment mode.
     */
    private final String id;

    /**
     * Constructs an {@code EnchantmentAttachment} enum constant.
     *
     * @param id The serialized string identifier.
     */
    EnchantmentAttachment(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this attachment mode.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}