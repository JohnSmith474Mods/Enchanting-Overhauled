package johnsmith.enchantingoverhauled.menu.scribe.model;

import johnsmith.enchantingoverhauled.api.tag.Tags;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public enum ScribeSlot {
    SOURCE(0, 8, 108),
    FUEL(1, 26, 108);

    // region Fields
    private final int index;
    private final int x;
    private final int y;
    // endregion

    // region Initialization
    /**
     * Constructs a ScribeSlot definition.
     *
     * @param index The underlying container slot index.
     * @param x     The X coordinate for rendering the slot in the GUI.
     * @param y     The Y coordinate for rendering the slot in the GUI.
     */
    ScribeSlot(int index, int x, int y) {
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
    public static Slot create(ScribeSlot slotType, Container container, ScribeGridData gridData) {
        return switch (slotType) {
            case SOURCE -> new Slot(container, slotType.index(), slotType.x(), slotType.y()) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.has(DataComponents.STORED_ENCHANTMENTS) && gridData.getGameState().ordinal() < ScribeGameState.PENDING.ordinal();
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPickup(@NotNull Player playerIn) {
                    return gridData.getGameState().ordinal() < ScribeGameState.PENDING.ordinal();
                }
            };
            case FUEL -> new Slot(container, slotType.index(), slotType.x(), slotType.y()) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.is(Tags.ENCHANTING_FUEL) && gridData.getGameState().ordinal() < ScribeGameState.PENDING.ordinal();
                }

                @Override
                public boolean mayPickup(@NotNull Player playerIn) {
                    return gridData.getGameState().ordinal() < ScribeGameState.PENDING.ordinal();
                }
            };
        };
    }
    // endregion
}
