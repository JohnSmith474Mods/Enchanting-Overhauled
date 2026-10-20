package johnsmith.enchantingoverhauled.client.network;

import johnsmith.enchantingoverhauled.menu.enchant.EnchantmentMenu;
import johnsmith.enchantingoverhauled.network.EnchantmentMenuSyncPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Handles the reception and execution of custom network payloads on the logical client.
 */
public class ClientPayloadHandler {

    // region Payload Handling
    /**
     * Processes the {@link EnchantmentMenuSyncPayload} received from the server.
     * <p>
     * Updates the client-side active {@link EnchantmentMenu} with the server-calculated list
     * of enchantment contexts. This task is enqueued onto the main client thread to ensure
     * thread safety and prevent {@link java.util.ConcurrentModificationException}s when
     * the UI reads this data during a render frame.
     *
     * @param payload The payload containing the container ID and the list of contexts.
     */
    public static void handleMenuSync(EnchantmentMenuSyncPayload payload) {
        // Enqueue onto the main client thread to prevent UI concurrent modification exceptions
        Minecraft.getInstance().execute(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            // Verify the target container is currently active before applying state
            if (player.containerMenu instanceof EnchantmentMenu menu && menu.containerId == payload.containerId()) {
                menu.setActiveContexts(payload.contexts());
            }
        });
    }
    // endregion
}