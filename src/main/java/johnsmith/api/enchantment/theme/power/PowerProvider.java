package johnsmith.api.enchantment.theme.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import johnsmith.api.enchantment.theme.power.bonus.PowerBonus;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.RegistryCodecs;

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
        RegistryEntryList<Block> blocks,
        int power,
        List<PowerBonus> bonuses
) {
    public static final Codec<PowerProvider> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("blocks").forGetter(PowerProvider::blocks),
                    Codec.INT.fieldOf("power").forGetter(PowerProvider::power),
                    PowerBonus.CODEC.listOf().optionalFieldOf("bonuses", List.of()).forGetter(PowerProvider::bonuses)
            ).apply(instance, PowerProvider::new)
    );
}