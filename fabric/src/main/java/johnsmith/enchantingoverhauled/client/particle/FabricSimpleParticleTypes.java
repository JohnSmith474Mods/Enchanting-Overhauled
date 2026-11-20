package johnsmith.enchantingoverhauled.client.particle;

import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

public final class FabricSimpleParticleTypes {
    public static void initialize() {
        ParticleProviderRegistry.getInstance().register(SimpleParticleTypes.SGA_RUNE.type(), SgaRuneParticle.Provider::new);
    }

    private FabricSimpleParticleTypes() {}
}