package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.MapCodec;
import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.util.Unit;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;

import java.util.function.UnaryOperator;

/**
 * Registry class for all custom {@link MapCodec} (server-side effects) and
 * {@link DataComponentType} (item-based effects/data) used by the mod's enchantment system.
 * <p>
 * This class ensures static initialization and centralizes the registration of all custom
 * enchantment-related components into Minecraft's built-in registries.
 */
public class EnchantmentEffectComponentRegistry {

    /**
     * Registers the effect to calculate and set a custom fire duration on an arrow entity.
     * Used for the Flame enchantment overhaul.
     */
    public static final MapCodec<SetFireDurationEffect> SET_FIRE_DURATION = register("set_fire_duration", SetFireDurationEffect.CODEC);

    /**
     * Registers the effect to scatter multiple lightning bolts on and around a target.
     * Used for the Channeling enchantment overhaul.
     */
    public static final MapCodec<ScatterLightningEffect> SCATTER_LIGHTNING = register("scatter_lightning", ScatterLightningEffect.CODEC);

    /**
     * Helper to register a custom {@link MapCodec} into the {@code ENCHANTMENT_ENTITY_EFFECT_TYPE} registry.
     *
     * @param name The resource path for the effect.
     * @param codec The codec instance to register.
     * @param <T> The type of the effect, extending {@link EnchantmentEntityEffect}.
     * @return The registered {@link MapCodec}.
     */
    private static <T extends EnchantmentEntityEffect> MapCodec<T> register(String name, MapCodec<T> codec) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                codec);
    }

    /**
     * Registers the component for custom binding curse behavior, allowing a chance to keep armor on death.
     */
    public static final DataComponentType<BindingCurseEffect> BINDING_CHANCE = register("binding_chance", builder -> builder.persistent(BindingCurseEffect.CODEC));

    /**
     * Registers the component for custom vanishing curse behavior, allowing a chance to keep item on death.
     */
    public static final DataComponentType<VanishingCurseEffect> VANISHING_CHANCE = register("vanishing_chance", builder -> builder.persistent(VanishingCurseEffect.CODEC));

    /**
     * Registers the component for probabilistic silk touch logic.
     */
    public static final DataComponentType<SilkTouchEffect> SILK_TOUCH = register("silk_touch", builder -> builder.persistent(SilkTouchEffect.CODEC));

    /**
     * Registers the component for clearing fluid fog based on enchantment level.
     */
    public static final DataComponentType<FluidVisibilityEffect> CLEAR_WATER_VISION = register("clear_fluid_vision", builder -> builder.persistent(FluidVisibilityEffect.CODEC));

    /**
     * Registers the component for modifying bow charge time, supporting effects like Quick Charge on bows.
     */
    public static final DataComponentType<BowChargeTimeEffect> BOW_CHARGE_TIME = register("bow_charge_time", builder -> builder.persistent(BowChargeTimeEffect.CODEC));

    /**
     * Registers a marker component indicating an item grants powder snow walkability, typically for boots.
     */
    public static final DataComponentType<Unit> POWDER_SNOW_WALKABLE = register("powder_snow_walkable", builder -> builder.persistent(Unit.CODEC));

    /**
     * Helper to register a custom {@link DataComponentType} into the {@code ENCHANTMENT_EFFECT_COMPONENT_TYPE} registry.
     *
     * @param name The resource path for the component.
     * @param builderOperator An operator that applies custom builders (like persistent codecs) to the component builder.
     * @param <T> The type of data stored in the component.
     * @return The registered {@link DataComponentType}.
     */
    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                builderOperator.apply(DataComponentType.builder()).build());
    }

    /**
     * Ensures this class is loaded and triggers static registration of all fields.
     */
    public static void initialize() {
    }
}