package johnsmith.enchantingoverhauled.particle;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class FabricSimpleParticleTypes {
    public static void initialize() {
        for (SimpleParticleTypes particle : SimpleParticleTypes.values()) {
            Identifier location = Identifier.fromNamespaceAndPath(Constants.MOD_ID, particle.getSerializedName());
            if (!BuiltInRegistries.PARTICLE_TYPE.containsKey(location)) {
                Registry.register(BuiltInRegistries.PARTICLE_TYPE, location, particle.type());
            }
        }
    }

    private FabricSimpleParticleTypes() {}
}
