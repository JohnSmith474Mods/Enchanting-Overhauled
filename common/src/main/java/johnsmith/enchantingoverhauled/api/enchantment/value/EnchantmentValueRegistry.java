package johnsmith.enchantingoverhauled.api.enchantment.value;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.value.configurable.ConfigAwareValue;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * Central registry for all custom {@link LevelBasedValue} implementations used by the mod's enchantment system.
 * <p>
 * This class ensures that custom value calculation types (e.g., diminishing returns, config-aware logic)
 * are properly registered into Minecraft's {@code ENCHANTMENT_LEVEL_BASED_VALUE_TYPE} registry, allowing them
 * to be referenced via JSON in enchantment data files.
 */
public class EnchantmentValueRegistry {
    /**
     * Initializes and registers all custom {@link LevelBasedValue} codecs.
     * <p>
     * This method is intended to be called during the mod's initialization phase.
     */
    public static void initialize() {
        register("diminishing_returns", DiminishingReturnsValue.CODEC);
        register("polynomial", PolynomialValue.CODEC);
        register("config_aware", ConfigAwareValue.CODEC);
        register("probabilistic", ProbabilisticValue.CODEC);
        register("negative", NegateValue.CODEC);
    }

    /**
     * Helper method to register a custom value codec into the appropriate Minecraft registry.
     *
     * @param name The resource path for the value type (e.g., "diminishing_returns").
     * @param codec The {@link MapCodec} instance to register.
     */
    private static void register(String name, MapCodec<? extends LevelBasedValue> codec) {
        Registry.register(BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), codec);
    }
}