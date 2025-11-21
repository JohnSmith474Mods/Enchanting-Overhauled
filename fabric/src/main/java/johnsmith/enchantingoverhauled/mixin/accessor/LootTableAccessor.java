package johnsmith.enchantingoverhauled.mixin.accessor;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Mixin accessor for the {@link LootTable} class.
 * <p>
 * This interface exposes the private {@code pools} field, allowing the mod to
 * retrieve and modify the list of loot pools associated with a loot table.
 * This is primarily used to inject custom loot pools (e.g., for the Enchanted Tome)
 * into existing vanilla loot tables.
 */
@Mixin(LootTable.class)
public interface LootTableAccessor {

    /**
     * Retrieves the list of loot pools currently in this loot table.
     *
     * @return A list of {@link LootPool}s.
     */
    @Accessor("pools")
    List<LootPool> enchanting_overhauled$getPools();

    /**
     * Sets the list of loot pools for this loot table.
     * <p>
     * This accessor is marked as {@link Mutable} because the target field is final,
     * allowing the mod to overwrite the list when injecting new pools.
     *
     * @param pools The new list of {@link LootPool}s to set.
     */
    @Mutable
    @Accessor("pools")
    void enchanting_overhauled$setPools(List<LootPool> pools);
}