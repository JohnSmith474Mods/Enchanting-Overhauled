package johnsmith.enchantingoverhauled.menu.scribe.controller;

/**
 * A utility record that encodes and decodes the Scribe Minigame interactions
 * into a single integer payload for native Container synchronization.
 */
public record ScribeInput(int x, int y, int action) {

    public static final int ACTION_REVEAL = 0;
    public static final int ACTION_FLAG = 1;
    public static final int ACTION_START = 2;
    public static final int ACTION_ABORT = 3;
    public static final int ACTION_ACK_LOSS = 4;
    public static final int ACTION_TAKE_BOOK = 5;
    public static final int ACTION_RETURN_TO_INDEX = 6;
    public static final int ACTION_SELECT_ENCHANTMENT = 7;

    /**
     * Encodes the interaction into a single integer payload.
     * Bits 0-15: X coordinate (or 16-bit Registry ID)
     * Bits 16-23: Y coordinate
     * Bits 24-31: Action Type
     */
    public int encode() {
        return (action << 24) | ((y & 0xFF) << 16) | (x & 0xFFFF);
    }

    /**
     * Decodes the integer payload received by the server back into an input record.
     */
    public static ScribeInput decode(int payload) {
        int x = payload & 0xFFFF;
        int y = (payload >> 16) & 0xFF;
        int action = (payload >> 24) & 0xFF;

        return new ScribeInput(x, y, action);
    }
}