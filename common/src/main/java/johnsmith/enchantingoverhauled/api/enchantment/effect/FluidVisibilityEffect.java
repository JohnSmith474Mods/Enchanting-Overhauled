package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.material.Fluid;

/**
 * A data-driven record defining an enchantment component effect that modifies fog/visibility
 * when the player is submerged in certain fluids.
 * <p>
 * This is primarily used by the Aqua Affinity enchantment to clear underwater visibility
 * beyond the vanilla range by adjusting the shader fog start and end distances.
 *
 * @param fluids              A set of fluid tags or IDs that this effect should apply to (e.g., {@code #minecraft:water}).
 * @param fogStart            A {@link LevelBasedValue} to calculate the start distance of the fog.
 * @param fogEndMultiplier    A {@link LevelBasedValue} to calculate a multiplier for the view distance
 * which determines the final end distance of the fog.
 */
public record FluidVisibilityEffect(HolderSet<Fluid> fluids, LevelBasedValue fogStart, LevelBasedValue fogEndMultiplier) {

    /**
     * The codec responsible for serializing and deserializing instances of this record from data files (e.g., JSON).
     * <p>
     * It uses {@link RegistryCodecs#homogeneousList} to properly handle the {@link HolderSet} of fluids.
     */
    public static final Codec<FluidVisibilityEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluids").forGetter(FluidVisibilityEffect::fluids),
            LevelBasedValue.CODEC.fieldOf("fog_start").forGetter(FluidVisibilityEffect::fogStart),
            LevelBasedValue.CODEC.fieldOf("fog_end_multiplier").forGetter(FluidVisibilityEffect::fogEndMultiplier)
    ).apply(instance, FluidVisibilityEffect::new));
}