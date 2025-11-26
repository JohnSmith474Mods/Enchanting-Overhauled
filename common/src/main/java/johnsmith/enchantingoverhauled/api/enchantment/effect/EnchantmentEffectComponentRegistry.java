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

public class EnchantmentEffectComponentRegistry {

    public static final MapCodec<SetFireDurationEffect> SET_FIRE_DURATION = register("set_fire_duration", SetFireDurationEffect.CODEC);
    public static final MapCodec<ScatterLightningEffect> SCATTER_LIGHTNING = register("scatter_lightning", ScatterLightningEffect.CODEC);

    private static <T extends EnchantmentEntityEffect> MapCodec<T> register(String name, MapCodec<T> codec) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                codec);
    }

    public static final DataComponentType<BindingCurseEffect> BINDING_CHANCE = register("binding_chance", builder -> builder.persistent(BindingCurseEffect.CODEC));
    public static final DataComponentType<VanishingCurseEffect> VANISHING_CHANCE = register("vanishing_chance", builder -> builder.persistent(VanishingCurseEffect.CODEC));
    public static final DataComponentType<SilkTouchEffect> SILK_TOUCH = register("silk_touch", builder -> builder.persistent(SilkTouchEffect.CODEC));
    public static final DataComponentType<FluidVisibilityEffect> CLEAR_WATER_VISION = register("clear_fluid_vision", builder -> builder.persistent(FluidVisibilityEffect.CODEC));
    public static final DataComponentType<BowChargeTimeEffect> BOW_CHARGE_TIME = register("bow_charge_time", builder -> builder.persistent(BowChargeTimeEffect.CODEC));
    public static final DataComponentType<Unit> POWDER_SNOW_WALKABLE = register("powder_snow_walkable", builder -> builder.persistent(Unit.CODEC));

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void initialize() {
        // Triggers static initialization
    }
}