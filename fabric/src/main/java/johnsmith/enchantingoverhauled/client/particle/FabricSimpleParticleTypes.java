package johnsmith.enchantingoverhauled.client.particle;

import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public final class FabricSimpleParticleTypes {
    public static void initialize() {
        ParticleFactoryRegistry.getInstance().register(SimpleParticleTypes.SGA_RUNE.type(), SgaRuneParticle.Provider::new);
    }

    private FabricSimpleParticleTypes() {}
}