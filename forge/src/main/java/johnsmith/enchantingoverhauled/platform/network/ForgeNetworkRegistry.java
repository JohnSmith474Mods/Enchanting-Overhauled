package johnsmith.enchantingoverhauled.platform.network;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.client.network.ClientPayloadHandler;
import johnsmith.enchantingoverhauled.network.EnchantmentMenuSyncPayload;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public final class ForgeNetworkRegistry {
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "main"))
            .networkProtocolVersion(1)
            .simpleChannel();

    public static void initialize() {
        CHANNEL.messageBuilder(EnchantmentMenuSyncPayload.class, NetworkDirection.PLAY_TO_CLIENT)
                .codec(EnchantmentMenuSyncPayload.STREAM_CODEC)
                .consumerMainThread((payload, context) -> ClientPayloadHandler.handleMenuSync(payload))
                .add();
    }

    private ForgeNetworkRegistry() {}
}