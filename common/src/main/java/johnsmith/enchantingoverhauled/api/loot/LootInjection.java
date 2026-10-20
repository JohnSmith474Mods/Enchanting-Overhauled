package johnsmith.enchantingoverhauled.api.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

/**
 * A data-driven record defining a loot table injection rule.
 * <p>
 * Specifies a list of target {@link LootTable} keys (such as vanilla chest loot tables)
 * and a list of injection {@link LootTable} keys whose contents will be appended as nested
 * loot pools to each target table upon server startup and datapack reload.
 *
 * @param targets    The list of destination {@link LootTable} resource keys to modify.
 * @param injections The list of {@link LootTable} resource keys to inject as nested pools into {@code targets}.
 * @see LootInjections
 * @see johnsmith.enchantingoverhauled.loot.LootInjectionHandler
 */
public record LootInjection(
        List<ResourceKey<LootTable>> targets,
        List<ResourceKey<LootTable>> injections
) {

    /**
     * The codec responsible for serializing and deserializing {@link LootInjection} definitions from JSON.
     */
    public static final Codec<LootInjection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceKey.codec(Registries.LOOT_TABLE).listOf().fieldOf("targets").forGetter(LootInjection::targets),
                    ResourceKey.codec(Registries.LOOT_TABLE).listOf().fieldOf("injections").forGetter(LootInjection::injections)
            ).apply(instance, LootInjection::new)
    );
}