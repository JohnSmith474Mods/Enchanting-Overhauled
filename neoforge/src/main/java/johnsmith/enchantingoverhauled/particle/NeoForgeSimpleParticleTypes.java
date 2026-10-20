package johnsmith.enchantingoverhauled.particle;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.registries.RegisterEvent;

public final class NeoForgeSimpleParticleTypes {
    public static void initialize(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.PARTICLE_TYPE.key())) {
            for (SimpleParticleTypes particle : SimpleParticleTypes.values()) {
                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, particle.getSerializedName());
                if (!BuiltInRegistries.PARTICLE_TYPE.containsKey(location)) {
                    Registry.register(BuiltInRegistries.PARTICLE_TYPE, location, particle.type());
                }
            }
        }
    }

    private NeoForgeSimpleParticleTypes() {}
}