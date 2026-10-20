package johnsmith.enchantingoverhauled.mixin.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to synchronize the Lectern's stored book to the client.
 * <p>
 * Vanilla Lecterns do not send their ItemStack to the client, leading to the client-side
 * rendering engine assuming the Lectern is empty or holding a default book. This ensures
 * our dynamic renderer knows exactly what is placed on the podium.
 */
@Mixin(LecternBlockEntity.class)
public abstract class LecternBlockEntityMixin extends BlockEntity {

    public LecternBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Vanilla saves the book into NBT via saveAdditional, which saveCustomOnly calls.
        return this.saveCustomOnly(registries);
    }

    /**
     * Intercepts whenever a book is placed or removed to trigger a network block update.
     */
    @Inject(method = "setBook", at = @At("TAIL"))
    private void enchanting_overhauled$syncBookToClient(ItemStack stack, CallbackInfo ci) {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }
}