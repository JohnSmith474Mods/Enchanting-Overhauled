package johnsmith.enchantingoverhauled.menu.scribe.model;

import net.minecraft.world.inventory.ContainerData;

/**
 * A strongly-typed wrapper around Minecraft's raw {@link ContainerData} integer array.
 * <p>
 * Centralizes all bit-shifting, mask logic, and index math for the Minesweeper grid,
 * allowing the Client (UI) and Server (Logic) to interact with the synchronized data safely.
 */
public class ScribeGridData {

    // region Constraints & Masks
    public static final int MAX_COLS = 22;
    public static final int MAX_ROWS = 11;
    public static final int TOTAL_CELLS = MAX_COLS * MAX_ROWS;

    public static final int MASK_MINE = 1;      // Bit 0
    public static final int MASK_REVEALED = 2;  // Bit 1
    public static final int MASK_FLAGGED = 4;   // Bit 2
    public static final int SHIFT_ADJACENT = 3; // Bits 3-6

    // Data Indices (appended after the grid cells)
    private static final int INDEX_STATE = TOTAL_CELLS;
    private static final int INDEX_IS_TOME = TOTAL_CELLS + 1;
    private static final int INDEX_MINE_COUNT = TOTAL_CELLS + 2;
    private static final int INDEX_XP_REWARD = TOTAL_CELLS + 3;
    private static final int INDEX_LAPIS_COST = TOTAL_CELLS + 4;
    private static final int INDEX_SELECTED_ENCHANTMENT = TOTAL_CELLS + 5;
    // endregion

    private final ContainerData data;

    public ScribeGridData(ContainerData data) {
        this.data = data;
    }

    // region State Accessors
    public ScribeGameState getGameState() {
        return ScribeGameState.byId(this.data.get(INDEX_STATE));
    }

    public void setGameState(ScribeGameState state) {
        this.data.set(INDEX_STATE, state.ordinal());
    }

    public boolean isTome() { return this.data.get(INDEX_IS_TOME) == 1; }
    public void setIsTome(boolean isTome) { this.data.set(INDEX_IS_TOME, isTome ? 1 : 0); }

    public int getMineCount() { return this.data.get(INDEX_MINE_COUNT); }
    public void setMineCount(int count) { this.data.set(INDEX_MINE_COUNT, count); }

    public int getXpReward() { return this.data.get(INDEX_XP_REWARD); }
    public void setXpReward(int reward) { this.data.set(INDEX_XP_REWARD, reward); }

    public int getLapisCost() { return this.data.get(INDEX_LAPIS_COST); }
    public void setLapisCost(int cost) { this.data.set(INDEX_LAPIS_COST, cost); }

    public int getSelectedEnchantmentIndex() { return this.data.get(INDEX_SELECTED_ENCHANTMENT); }
    public void setSelectedEnchantmentIndex(int index) { this.data.set(INDEX_SELECTED_ENCHANTMENT, index); }
    // endregion

    // region Cell Accessors
    public int getCellState(int x, int y) {
        return this.data.get(getIndex(x, y));
    }

    public void setCellState(int x, int y, int state) {
        this.data.set(getIndex(x, y), state);
    }

    public boolean hasMine(int x, int y) {
        return (getCellState(x, y) & MASK_MINE) != 0;
    }

    public boolean isRevealed(int x, int y) {
        return (getCellState(x, y) & MASK_REVEALED) != 0;
    }

    public boolean isFlagged(int x, int y) {
        return (getCellState(x, y) & MASK_FLAGGED) != 0;
    }

    public int getAdjacentMines(int x, int y) {
        return (getCellState(x, y) >> SHIFT_ADJACENT) & 0xF;
    }

    public void setMask(int x, int y, int mask) {
        setCellState(x, y, getCellState(x, y) | mask);
    }

    public void clearMask(int x, int y, int mask) {
        setCellState(x, y, getCellState(x, y) & ~mask);
    }

    public void setAdjacentMines(int x, int y, int count) {
        int state = getCellState(x, y) & ~(0xF << SHIFT_ADJACENT); // Clear existing
        setCellState(x, y, state | (count << SHIFT_ADJACENT));
    }

    public int getIndex(int x, int y) {
        return y * MAX_COLS + x;
    }
    // endregion
}