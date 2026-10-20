package johnsmith.enchantingoverhauled.client.particle;

import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

public final class ForgeSimpleParticleTypes {

    public static void initialize(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(SimpleParticleTypes.SGA_RUNE.type(), SgaRuneParticle.Provider::new);
    }

    private ForgeSimpleParticleTypes() {}
}