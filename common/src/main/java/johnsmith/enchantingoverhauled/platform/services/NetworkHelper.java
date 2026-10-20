package johnsmith.enchantingoverhauled.platform.services;

import johnsmith.enchantingoverhauled.network.EnchantmentMenuSyncPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Defines the contract for cross-platform network communication and payload dispatching.
 */
public interface NetworkHelper {

    /**
     * Dispatches a custom network payload to a specific client.
     * <p>
     * Used to synchronize complex, non-primitive menu state (like the EnchantmentContext list)
     * that cannot be natively synchronized via standard container DataSlots.
     *
     * @param payload      The constructed payload containing the data to synchronize.
     * @param serverPlayer The specific connected client player to receive the payload.
     */
    void sendToClient(EnchantmentMenuSyncPayload payload, ServerPlayer serverPlayer);
}