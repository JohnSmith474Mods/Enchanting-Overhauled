package johnsmith.enchantingoverhauled.menu.enchant.model;

import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.Optional;

/**
 * An immutable data record representing the logical back-end state of a single
 * generated enchantment option within the Enchanting Altar.
 * <p>
 * This context object holds the raw functional data (costs, requirements, instances)
 * before it is processed into a visual render state for the UI.
 *
 * @param slotIndex      The specific index order of this context in the generated list.
 *                       Determines determinism for texture variation and tracking.
 * @param applied        The existing enchantment instance currently present on the target item, if any.
 * @param pending        The proposed or incoming enchantment instance and its origin source.
 * @param xpRequirement  The total enchanting power (e.g., bookshelf count) needed to unlock this option.
 * @param lapisCost      The amount of Lapis Lazuli consumed upon executing this action.
 * @param xpCost         The amount of experience levels consumed upon executing this action.
 */
public record EnchantContext(
        int slotIndex,
        Optional<EnchantmentInstance> applied,
        PendingEnchantment pending,
        int xpRequirement,
        int lapisCost,
        int xpCost
) {
    // region Overrides
    /**
     * Returns a string representation of this context, unwrapping the inner optionals
     * for easier debugging and logging.
     *
     * @return The formatted string.
     */
    @Override
    public String toString() {
        String appStr = applied.map(e -> e.enchantment.unwrapKey().map(k -> k.location().toString()).orElse("unknown") + " (Lvl " + e.level + ")").orElse("empty");
        return "EnchantContext[slot=" + slotIndex + ", applied=" + appStr + ", pending=" + pending + ", xpReq=" + xpRequirement + ", lapis=" + lapisCost + ", xpCost=" + xpCost + "]";
    }
    // endregion
}