package johnsmith.enchantingoverhauled.api.enchantment.value;

import johnsmith.enchantingoverhauled.Constants;

import com.mojang.serialization.MapCodec;

import johnsmith.enchantingoverhauled.api.enchantment.value.configurable.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public class EnchantmentValueRegistry {
    public static void initialize() {
        register("diminishing_returns", DiminishingReturnsValue.CODEC);
        register("polynomial", PolynomialValue.CODEC);
        register("config_aware", ConfigAwareValue.CODEC);
        register("probabilistic", ProbabilisticValue.CODEC);
        register("negative", NegateValue.CODEC);
    }

    private static void register(String name, MapCodec<? extends LevelBasedValue> codec) {
        Registry.register(BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), codec);
    }
}
