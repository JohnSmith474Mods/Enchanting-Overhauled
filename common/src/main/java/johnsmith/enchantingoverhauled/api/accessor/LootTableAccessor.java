package johnsmith.enchantingoverhauled.api.accessor;

import net.minecraft.world.level.storage.loot.LootPool;

import java.util.List;

/**
 * Accessor interface for the {@link net.minecraft.world.level.storage.loot.LootTable} class.
 * <p>
 * This interface is implemented via Mixin to expose the private {@code pools} field on a loot table,
 * allowing Enchanting Overhauled to inspect and replace the list of {@link LootPool}s at runtime.
 * This is primarily used by the loot injection system to append custom pools (such as Enchanted Tomes
 * and Enchanted Pages) into existing vanilla or modded loot tables upon server start and datapack reload.
 *
 * @see johnsmith.enchantingoverhauled.api.loot.LootInjection
 * @see johnsmith.enchantingoverhauled.loot.LootInjectionHandler
 */
public interface LootTableAccessor {

    /**
     * Retrieves the list of loot pools currently configured in this loot table.
     *
     * @return The current list of {@link LootPool} instances.
     */
    List<LootPool> enchanting_overhauled$getPools();

    /**
     * Replaces the list of loot pools for this loot table.
     *
     * @param pools The new list of {@link LootPool} instances to assign to the loot table.
     */
    void enchanting_overhauled$setPools(List<LootPool> pools);
}