package johnsmith.enchantingoverhauled.api.enchantment.value;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Global context holder providing the active world {@link Difficulty} for {@link DifficultyBasedValue}
 * evaluations where a {@link net.minecraft.world.level.Level} reference is not directly passed by vanilla's
 * {@link net.minecraft.world.item.enchantment.LevelBasedValue#calculate(int)} signature.
 */
public final class DifficultyContext {

    /**
     * Reference to the currently running {@link MinecraftServer}, or {@code null} if no server is active.
     */
    @Nullable
    private static MinecraftServer currentServer;

    /**
     * Client-side fallback supplier used when a {@link DifficultyBasedValue} is evaluated on a logical client
     * without an integrated server (defaults to {@link Difficulty#NORMAL}).
     */
    private static Supplier<Difficulty> clientFallback = () -> Difficulty.NORMAL;

    /**
     * Private constructor to prevent instantiation.
     */
    private DifficultyContext() {}

    /**
     * Sets or clears the active {@link MinecraftServer} reference on server startup/shutdown.
     *
     * @param server The active {@link MinecraftServer}, or {@code null} when the server stops.
     */
    public static void setServer(@Nullable MinecraftServer server) {
        currentServer = server;
    }

    /**
     * Sets the client-side fallback supplier used to query difficulty from the client level.
     *
     * @param fallback A supplier returning the client's current {@link Difficulty}.
     */
    public static void setClientFallback(Supplier<Difficulty> fallback) {
        clientFallback = fallback;
    }

    /**
     * Resolves the current world {@link Difficulty}, preferring the active server's world data
     * and falling back to the client difficulty supplier.
     *
     * @return The active {@link Difficulty}.
     */
    public static Difficulty getDifficulty() {
        MinecraftServer server = currentServer;
        if (server != null) {
            return server.getWorldData().getDifficulty();
        }
        return clientFallback.get();
    }
}