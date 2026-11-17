package johnsmith.enchantingoverhauled.api.enchantment.theme.power;

import johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus.PowerBonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;

import java.util.List;

/**
 * A data-driven record that defines a rule for how a specific block or group of blocks
 * contributes enchanting power toward a theme (e.g., a Bookshelf contributes 1 power to the Default theme).
 * <p>
 * This is the core data unit used by the enchanting table logic to determine available thematic power.
 *
 * @param blocks  A {@link HolderSet} representing the block(s) or block tag that provide power for the theme.
 * @param power   The base integer amount of enchanting power this provider grants.
 * @param bonuses A list of optional {@link PowerBonus} rules that apply conditional or dynamic modifications
 * to the base power value.
 */
public record PowerProvider(
        HolderSet<Block> blocks,
        int power,
        List<PowerBonus> bonuses
) {
    /**
     * The codec responsible for serializing and deserializing instances of this record from data files (e.g., JSON).
     * <p>
     * It uses {@link RegistryCodecs#homogeneousList} to correctly handle the required {@link HolderSet} of blocks.
     * The {@code bonuses} field defaults to an empty list if omitted.
     */
    public static final Codec<PowerProvider> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(PowerProvider::blocks),
                    Codec.INT.fieldOf("power").forGetter(PowerProvider::power),
                    PowerBonus.CODEC.listOf().optionalFieldOf("bonuses", List.of()).forGetter(PowerProvider::bonuses)
            ).apply(instance, PowerProvider::new)
    );
}