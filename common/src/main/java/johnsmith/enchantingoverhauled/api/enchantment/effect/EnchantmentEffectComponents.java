package johnsmith.enchantingoverhauled.api.enchantment.effect;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Enumeration of custom enchantment effect {@link DataComponentType}s introduced by Enchanting Overhauled.
 * <p>
 * These components are registered to {@link net.minecraft.core.registries.BuiltInRegistries#ENCHANTMENT_EFFECT_COMPONENT_TYPE}
 * and allow enchantments to define probabilistic behavior for Binding Curse, Vanishing Curse, and Silk Touch.
 */
public enum EnchantmentEffectComponents implements StringRepresentable {

    /**
     * Enchantment effect component governing the probability that an equipped armor piece with
     * Curse of Binding is retained across death and respawn.
     */
    BINDING_CHANCE("binding_chance", DataComponentType.<BindingCurseEffect>builder().persistent(BindingCurseEffect.CODEC).build()),

    /**
     * Enchantment effect component governing the probability that an item with Curse of Vanishing
     * is destroyed upon player death.
     */
    VANISHING_CHANCE("vanishing_chance", DataComponentType.<VanishingCurseEffect>builder().persistent(VanishingCurseEffect.CODEC).build()),

    /**
     * Enchantment effect component governing the probability that Silk Touch preserves the mined block intact.
     */
    SILK_TOUCH("silk_touch", DataComponentType.<SilkTouchEffect>builder().persistent(SilkTouchEffect.CODEC).build());

    /**
     * The registry path identifier for this enchantment effect component.
     */
    private final String id;

    /**
     * The underlying {@link DataComponentType} instance.
     */
    private final DataComponentType<?> type;

    /**
     * Constructs an {@code EnchantmentEffectComponents} constant.
     *
     * @param id   The serialized registry name.
     * @param type The constructed {@link DataComponentType}.
     */
    EnchantmentEffectComponents(String id, DataComponentType<?> type) {
        this.id = id;
        this.type = type;
    }

    /**
     * Retrieves the underlying {@link DataComponentType} for this enchantment effect component.
     *
     * @return The {@link DataComponentType} instance.
     */
    public DataComponentType<?> get() {
        return this.type;
    }

    /**
     * Retrieves the serialized registry path name for this component.
     *
     * @return The string identifier.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}