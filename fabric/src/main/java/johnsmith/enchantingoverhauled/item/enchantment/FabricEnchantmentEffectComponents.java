package johnsmith.enchantingoverhauled.item.enchantment;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class FabricEnchantmentEffectComponents {
    public static void initialize() {
        for (EnchantmentEffectComponents component : EnchantmentEffectComponents.values()) {
            Registry.register(
                    BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, component.getSerializedName()),
                    component.get()
            );
        }
    }

    private FabricEnchantmentEffectComponents() {}
}
