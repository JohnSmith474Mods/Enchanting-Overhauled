package johnsmith.enchantingoverhauled.particle;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraftforge.registries.RegisterEvent;

public final class ForgeSimpleParticleTypes {
    public static void initialize(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.PARTICLE_TYPE.key())) {
            for (SimpleParticleTypes particle : SimpleParticleTypes.values()) {
                Identifier location = Identifier.fromNamespaceAndPath(Constants.MOD_ID, particle.getSerializedName());
                if (!BuiltInRegistries.PARTICLE_TYPE.containsKey(location)) {
                    event.register(
                            BuiltInRegistries.PARTICLE_TYPE.key(),
                            location,
                            particle::type
                    );
                }
            }
        }
    }

    private ForgeSimpleParticleTypes() {}
}