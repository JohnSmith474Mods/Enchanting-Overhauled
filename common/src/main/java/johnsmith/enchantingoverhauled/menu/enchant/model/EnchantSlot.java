package johnsmith.enchantingoverhauled.menu.enchant.model;

import johnsmith.enchantingoverhauled.api.tag.Tags;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

/**
 * An enumeration defining the exact indices and screen coordinates of the interactable
 * item slots utilized within the custom Enchanting Altar menu.
 * <p>
 * Centralizes these magic numbers to prevent drift and ensure consistency between
 * container backend logic and UI frontend rendering.
 */
public enum EnchantSlot {

    // region Constants
    /**
     * The primary target item slot (e.g., a Sword, Pickaxe, or Book).
     * Mapped to index 0.
     */
    TARGET(0, 18, 42),

    /**
     * The fuel slot requiring Lapis Lazuli to execute enchantments.
     * Mapped to index 1.
     */
    FUEL(1, 28, 65),

    /**
     * The catalyst source slot, exclusively accepting Enchanted Books/Tomes.
     * Mapped to index 2.
     */
    SOURCE(2, 8, 65);
    // endregion

    // region Fields
    private final int index;
    private final int x;
    private final int y;
    // endregion

    // region Initialization
    /**
     * Constructs an EnchantSlot definition.
     *
     * @param index The underlying container slot index.
     * @param x     The X coordinate for rendering the slot in the GUI.
     * @param y     The Y coordinate for rendering the slot in the GUI.
     */
    EnchantSlot(int index, int x, int y) {
        this.index = index;
        this.x = x;
        this.y = y;
    }
    // endregion

    // region Accessors
    /**
     * Retrieves the backend container index associated with this slot.
     *
     * @return The slot integer index.
     */
    public int index() {
        return index;
    }

    /**
     * Retrieves the visual X coordinate for the slot within the UI screen.
     *
     * @return The X coordinate in pixels.
     */
    public int x() {
        return x;
    }

    /**
     * Retrieves the visual Y coordinate for the slot within the UI screen.
     *
     * @return The Y coordinate in pixels.
     */
    public int y() {
        return y;
    }
    // endregion

    // region Factory
    /**
     * Generates a new inventory slot tailored with specific placement and stacking rules
     * according to its designated slot type.
     *
     * @param slotType  The enum classification defining the slot's index, coordinates, and rules.
     * @param container The underlying inventory container the slot will interact with.
     * @return A newly constructed {@link Slot} implementation.
     */
    public static Slot create(EnchantSlot slotType, Container container) {
        return switch (slotType) {
            case TARGET -> new Slot(container, slotType.index(), slotType.x(), slotType.y()) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            };
            case FUEL -> new Slot(container, slotType.index(), slotType.x(), slotType.y()) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.is(Tags.ENCHANTING_FUEL);
                }
            };
            case SOURCE -> new Slot(container, slotType.index(), slotType.x(), slotType.y()) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.has(DataComponents.STORED_ENCHANTMENTS);
                }
            };
        };
    }
    // endregion
}