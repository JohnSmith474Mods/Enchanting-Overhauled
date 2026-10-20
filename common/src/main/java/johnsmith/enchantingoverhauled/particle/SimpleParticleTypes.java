package johnsmith.enchantingoverhauled.particle;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Common particle type definitions for Enchanting Overhauled.
 * <p>
 * Keeps client-only {@code ParticleProvider} classes out of common code so dedicated servers
 * never load {@code net.minecraft.client.particle.*} classes, and defers {@link BuiltInRegistries#PARTICLE_TYPE}
 * registration to {@link #initialize()}.
 */
public enum SimpleParticleTypes implements StringRepresentable {
    SGA_RUNE("enchant", new SimpleParticleType(true) {});

    private final String id;
    private final SimpleParticleType type;

    SimpleParticleTypes(String id, SimpleParticleType type) {
        this.id = id;
        this.type = type;
    }

    public static void initialize() {
        for (SimpleParticleTypes particle : values()) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, particle.id);
            if (!BuiltInRegistries.PARTICLE_TYPE.containsKey(location)) {
                Registry.register(BuiltInRegistries.PARTICLE_TYPE, location, particle.type);
            }
        }
    }

    public SimpleParticleType type() {
        return this.type;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}