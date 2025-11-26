package johnsmith.enchantingoverhauled.accessor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Accessor interface for {@link net.minecraft.world.inventory.EnchantmentMenu}.
 * <p>
 * This interface is implemented via Mixin on the {@code EnchantmentMenu} class.
 * It exposes internal inventory slots, logic states, and rendering data (like texture indices)
 * so that the client-side {@code EnchantmentScreen} can render the overhauled GUI correctly.
 */
public interface EnchantmentMenuAccessor {

    /**
     * Gets the item stack currently residing in the target (enchantable item) slot.
     *
     * @return The target item stack.
     */
    public abstract ItemStack enchanting_overhauled$getEnchantmentTarget();

    /**
     * Gets the item stack currently residing in the source (modifier/book) slot.
     *
     * @return The source item stack.
     */
    public abstract ItemStack enchanting_overhauled$getEnchantmentSources();

    /**
     * Gets the array tracking the origin source for each of the three enchantment options.
     * <p>
     * The values correspond to:
     * <ul>
     * <li>{@code TARGET (0)}: Upgrade from the item itself.</li>
     * <li>{@code SOURCE (1)}: Transfer from the source item.</li>
     * <li>{@code TABLE (2)}: New enchantment from the table.</li>
     * </ul>
     *
     * @return An integer array of size 3 containing source IDs.
     */
    public abstract int[] enchanting_overhauled$getEnchantmentSourceArray();

    /**
     * Calculates the resource cost (Experience Levels and Lapis Lazuli) required
     * to apply the given enchantment.
     *
     * @param enchantment The enchantment to calculate the cost for.
     * @return The cost amount.
     */
    public abstract int enchanting_overhauled$calculateEnchantmentCost(Enchantment enchantment);

    /**
     * Gets the array of random texture indices used to render the background of
     * "Target" (Upgrade) buttons.
     *
     * @return An integer array of size 3 containing texture IDs (0-9).
     */
    public int[] enchanting_overhauled$getTargetTextureIndices();

    /**
     * Gets the array of random texture indices used to render the background of
     * "Source" (Transfer) buttons.
     *
     * @return An integer array of size 3 containing texture IDs (0-9).
     */
    public int[] enchanting_overhauled$getSourceTextureIndices();

    /**
     * Gets the array of random texture indices used to render the background of
     * "Table" (New Enchantment) buttons.
     *
     * @return An integer array of size 3 containing texture IDs (0-9).
     */
    public int[] enchanting_overhauled$getTableTextureIndices();
}