package johnsmith.enchantingoverhauled.mixin.block.entity;

import johnsmith.enchantingoverhauled.api.accessor.TomeStorageAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects tome storage capabilities into the vanilla EnchantingTableBlockEntity.
 * Manages NBT serialization and server-to-client data synchronization.
 */
@Mixin(EnchantingTableBlockEntity.class)
public abstract class EnchantingTableBlockEntityMixin extends BlockEntity implements TomeStorageAccessor {

    /**
     * Initializes a new block entity mixin instance.
     *
     * @param type  The block entity type.
     * @param pos   The block position.
     * @param state The block state.
     */
    public EnchantingTableBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * The stored tome item stack.
     */
    @Unique
    private ItemStack enchanting_overhauled$tomeStack = ItemStack.EMPTY;

    /**
     * Retrieves the stored tome item stack.
     *
     * @return The stored item stack.
     */
    @Override
    public ItemStack enchanting_overhauled$getTomeStack() {
        return this.enchanting_overhauled$tomeStack;
    }

    /**
     * Assigns a new tome item stack.
     * Marks the block entity as changed.
     * Dispatches a block update to synchronize data with the client.
     *
     * @param stack The item stack to store.
     */
    @Override
    public void enchanting_overhauled$setTomeStack(ItemStack stack) {
        this.enchanting_overhauled$tomeStack = stack;
        this.setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    /**
     * Writes the stored tome item stack to the NBT compound tag.
     *
     * @param tag        The NBT compound tag.
     * @param registries The registry access provider.
     * @param ci         The callback info.
     */
    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void serializeTomeStack(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (!this.enchanting_overhauled$tomeStack.isEmpty()) {
            tag.put("enchanting_overhauled:tome_stack", this.enchanting_overhauled$tomeStack.saveOptional(registries));
        }
    }

    /**
     * Reads the stored tome item stack from the NBT compound tag.
     *
     * @param tag        The NBT compound tag.
     * @param registries The registry access provider.
     * @param ci         The callback info.
     */
    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void deserializeTomeStack(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (tag.contains("enchanting_overhauled:tome_stack")) {
            this.enchanting_overhauled$tomeStack = ItemStack.parseOptional(registries, tag.getCompound("enchanting_overhauled:tome_stack"));
        } else {
            this.enchanting_overhauled$tomeStack = ItemStack.EMPTY;
        }
    }

    /**
     * Generates the network packet required to synchronize the block entity data to the client.
     *
     * @return The client-bound block entity data packet.
     */
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Constructs the NBT compound tag for the update packet.
     *
     * @param registries The registry access provider.
     * @return The NBT compound tag containing custom data.
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}