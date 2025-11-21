package johnsmith.enchantingoverhauled.api.enchantment.theme.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus.PowerBonus;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;

import java.util.List; // Import this

/**
 * A record that associates a Block (or tag of blocks) with the
 * enchanting power it provides for a specific theme.
 *
 * @param blocks The block or tag of blocks that provide power.
 * @param power The base amount of power this provider gives.
 * @param bonuses A list of optional, additional power calculations.
 */
public record PowerProvider(
        HolderSet<Block> blocks,
        int power,
        List<PowerBonus> bonuses
) {
    public static final Codec<PowerProvider> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(PowerProvider::blocks),
                    Codec.INT.fieldOf("power").forGetter(PowerProvider::power),
                    PowerBonus.CODEC.listOf().optionalFieldOf("bonuses", List.of()).forGetter(PowerProvider::bonuses)
            ).apply(instance, PowerProvider::new)
    );
}