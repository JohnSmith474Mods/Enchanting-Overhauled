package johnsmith.enchantingoverhauled.client.particle;

import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public final class NeoForgeSimpleParticleTypes {
    public static void initialize(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(SimpleParticleTypes.SGA_RUNE.type(), SgaRuneParticle.Provider::new);
    }

    private NeoForgeSimpleParticleTypes() {}
}