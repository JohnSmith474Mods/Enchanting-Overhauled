package johnsmith.enchantingoverhauled.api.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.material.Fluid;

public record FluidVisibilityEffect(HolderSet<Fluid> fluids, LevelBasedValue fogStart, LevelBasedValue fogEndMultiplier) {

    public static final Codec<FluidVisibilityEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluids").forGetter(FluidVisibilityEffect::fluids),
            LevelBasedValue.CODEC.fieldOf("fog_start").forGetter(FluidVisibilityEffect::fogStart),
            LevelBasedValue.CODEC.fieldOf("fog_end_multiplier").forGetter(FluidVisibilityEffect::fogEndMultiplier)
    ).apply(instance, FluidVisibilityEffect::new));
}