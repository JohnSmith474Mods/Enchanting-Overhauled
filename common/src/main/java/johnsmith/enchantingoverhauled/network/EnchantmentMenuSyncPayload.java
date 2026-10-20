package johnsmith.enchantingoverhauled.network;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantContext;
import johnsmith.enchantingoverhauled.menu.enchant.model.PendingEnchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A custom network payload responsible for synchronizing the complex, dynamically generated
 * list of {@link EnchantContext}s from the server to the client's enchanting menu.
 *
 * @param containerId The ID of the container menu receiving the sync.
 * @param contexts    The list of calculated enchantment contexts to display in the UI.
 */
public record EnchantmentMenuSyncPayload(int containerId, List<EnchantContext> contexts) implements CustomPacketPayload {

    // region Constants
    /**
     * The unique identifier type for this custom payload.
     */
    public static final Type<EnchantmentMenuSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchantment_menu_sync"));
    // endregion

    // region Codecs
    /**
     * A stream codec for serializing and deserializing {@link EnchantmentInstance} objects.
     * <p>
     * Safely unwraps the {@link Holder} into a {@link ResourceLocation} for network transit
     * to prevent crashes associated with transmitting unbound direct holders.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentInstance> ENCHANTMENT_INSTANCE_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, EnchantmentInstance instance) {
            // Safely unwrap the ResourceKey to prevent crashes from unbound Holder.Direct instances
            ResourceLocation loc = instance.enchantment.unwrapKey()
                    .map(ResourceKey::location)
                    .orElseGet(() -> ResourceLocation.withDefaultNamespace("unknown"));

            ResourceLocation.STREAM_CODEC.encode(buf, loc);
            buf.writeVarInt(instance.level);
        }

        @Override
        public EnchantmentInstance decode(RegistryFriendlyByteBuf buf) {
            ResourceLocation loc = ResourceLocation.STREAM_CODEC.decode(buf);
            int level = buf.readVarInt();
            Holder<Enchantment> holder = buf.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolder(ResourceKey.create(Registries.ENCHANTMENT, loc))
                    .orElseThrow();
            return new EnchantmentInstance(holder, level);
        }
    };

    /**
     * A stream codec for serializing and deserializing {@link PendingEnchantment} objects.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, PendingEnchantment> PENDING_ENCHANTMENT_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, PendingEnchantment pending) {
            buf.writeBoolean(pending.enchantment().isPresent());
            pending.enchantment().ifPresent(inst -> ENCHANTMENT_INSTANCE_CODEC.encode(buf, inst));
            buf.writeVarInt(pending.source().getId());
        }

        @Override
        public PendingEnchantment decode(RegistryFriendlyByteBuf buf) {
            Optional<EnchantmentInstance> enchantment = buf.readBoolean()
                    ? Optional.of(ENCHANTMENT_INSTANCE_CODEC.decode(buf))
                    : Optional.empty();
            EnchantmentSource source = EnchantmentSource.byId(buf.readVarInt());
            return new PendingEnchantment(enchantment, source);
        }
    };

    /**
     * A stream codec for serializing and deserializing full {@link EnchantContext} objects.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantContext> CONTEXT_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, EnchantContext context) {
            buf.writeVarInt(context.slotIndex());

            buf.writeBoolean(context.applied().isPresent());
            context.applied().ifPresent(inst -> ENCHANTMENT_INSTANCE_CODEC.encode(buf, inst));

            PENDING_ENCHANTMENT_CODEC.encode(buf, context.pending());

            buf.writeVarInt(context.xpRequirement());
            buf.writeVarInt(context.lapisCost());
            buf.writeVarInt(context.xpCost());
        }

        @Override
        public EnchantContext decode(RegistryFriendlyByteBuf buf) {
            int slotIndex = buf.readVarInt();
            Optional<EnchantmentInstance> applied = buf.readBoolean()
                    ? Optional.of(ENCHANTMENT_INSTANCE_CODEC.decode(buf))
                    : Optional.empty();
            PendingEnchantment pending = PENDING_ENCHANTMENT_CODEC.decode(buf);
            int xpRequirement = buf.readVarInt();
            int lapisCost = buf.readVarInt();
            int xpCost = buf.readVarInt();

            return new EnchantContext(slotIndex, applied, pending, xpRequirement, lapisCost, xpCost);
        }
    };

    /**
     * The primary stream codec for the entire {@link EnchantmentMenuSyncPayload}.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentMenuSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, EnchantmentMenuSyncPayload payload) {
            buf.writeVarInt(payload.containerId());
            ByteBufCodecs.collection(ArrayList::new, CONTEXT_CODEC).encode(buf, new ArrayList<>(payload.contexts()));
        }

        @Override
        public EnchantmentMenuSyncPayload decode(RegistryFriendlyByteBuf buf) {
            int containerId = buf.readVarInt();
            List<EnchantContext> contexts = ByteBufCodecs.collection(ArrayList::new, CONTEXT_CODEC).decode(buf);
            return new EnchantmentMenuSyncPayload(containerId, contexts);
        }
    };
    // endregion

    // region Implementation
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    // endregion
}