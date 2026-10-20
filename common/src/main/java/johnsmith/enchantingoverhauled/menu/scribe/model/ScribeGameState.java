package johnsmith.enchantingoverhauled.menu.scribe.model;

/**
 * Enumerates the high-level states of the Scribe Minigame.
 * Synchronized across the network via integer ordinal mapping in the ContainerData.
 */
public enum ScribeGameState {
    MISSING_SOURCE,
    READY,
    SATURATED,
    MAXIMIZED,
    PENDING,
    PLAYING,
    WON,
    LOST;

    private static final ScribeGameState[] VALUES = values();

    public static ScribeGameState byId(int id) {
        if (id < 0 || id >= VALUES.length) return MISSING_SOURCE;
        return VALUES[id];
    }
}