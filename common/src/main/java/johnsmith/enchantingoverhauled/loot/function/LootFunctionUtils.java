package johnsmith.enchantingoverhauled.loot.function;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;
import java.util.Optional;

/**
 * Shared utility operations for the custom enchanting loot functions.
 */
public class LootFunctionUtils {

    /**
     * Resolves the list of valid candidate enchantments based on explicit options or vanilla random tags.
     *
     * @param context       The loot generation context.
     * @param options       An optional predefined list of allowed enchantments.
     * @param allowTreasure Whether to allow enchantments tagged with 'TREASURE'.
     * @return A list of valid enchantment holders.
     */
    public static List<Holder<Enchantment>> getCandidates(LootContext context, Optional<HolderSet<Enchantment>> options, boolean allowTreasure) {
        if (options.isPresent()) {
            return options.get().stream().toList();
        } else {
            var registry = context.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            var tag = registry.getTag(EnchantmentTags.ON_RANDOM_LOOT);

            // Map the stream of Holder.Reference<Enchantment> explicitly to Holder<Enchantment>
            List<Holder<Enchantment>> all = tag.map(holders -> holders.stream().toList())
                    .orElseGet(() -> registry.holders().map(h -> (Holder<Enchantment>) h).toList());

            if (!allowTreasure) {
                return all.stream().filter(h -> !h.is(EnchantmentTags.TREASURE)).toList();
            }
            return all;
        }
    }
}