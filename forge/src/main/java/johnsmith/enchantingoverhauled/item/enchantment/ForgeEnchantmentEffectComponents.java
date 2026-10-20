package johnsmith.enchantingoverhauled.item.enchantment;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;

public final class ForgeEnchantmentEffectComponents {

    public static void initialize(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.key())) {
            for (EnchantmentEffectComponents component : EnchantmentEffectComponents.values()) {
                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, component.getSerializedName());
                if (!BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.containsKey(location)) {
                    event.register(
                            BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.key(),
                            location,
                            component::get
                    );
                }
            }
        }
    }

    private ForgeEnchantmentEffectComponents() {}
}