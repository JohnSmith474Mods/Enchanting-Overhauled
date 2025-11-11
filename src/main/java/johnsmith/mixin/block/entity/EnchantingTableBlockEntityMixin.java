package johnsmith.mixin.block.entity;

import johnsmith.mixin.accessor.EnchantingTableBlockEntityAccessor;
import johnsmith.accessor.TomeStorageAccessor;
import johnsmith.lib.EnchantmentLib;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantingTableBlockEntity.class)
public abstract class EnchantingTableBlockEntityMixin extends BlockEntity implements TomeStorageAccessor {

    @Unique
    private ItemStack enchanting_overhauled$tomeStack = ItemStack.EMPTY;

    // Required constructor
    public EnchantingTableBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --- Accessor Implementation ---
    @Override
    public ItemStack enchanting_overhauled$getTomeStack() {
        return this.enchanting_overhauled$tomeStack;
    }

    @Override
    public void enchanting_overhauled$setTomeStack(ItemStack stack) {
        this.enchanting_overhauled$tomeStack = stack;
        this.markDirty();
    }

    // --- NBT (Save/Load) ---
    @Inject(method = "writeNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)V", at = @At("RETURN"))
    private void writeTomeStackNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        if (!this.enchanting_overhauled$tomeStack.isEmpty()) {
            nbt.put("enchanting_overhauled_tome", this.enchanting_overhauled$tomeStack.encode(registryLookup));
        }
    }

    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)V", at = @At("RETURN"))
    private void readTomeStackNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        if (nbt.contains("enchanting_overhauled_tome")) {
            this.enchanting_overhauled$tomeStack = ItemStack.fromNbt(registryLookup, nbt.get("enchanting_overhauled_tome")).orElse(ItemStack.EMPTY);
        } else {
            this.enchanting_overhauled$tomeStack = ItemStack.EMPTY;
        }
    }

    // --- Injection for Particles ---
    @Inject(method = "tick", at = @At("TAIL"))
    private static void onClientTick(World world, BlockPos pos, BlockState state, EnchantingTableBlockEntity blockEntity, CallbackInfo ci) {

        // Check if a player is close enough to open the book
        PlayerEntity playerEntity = world.getClosestPlayer(
                (double)pos.getX() + 0.5D,
                (double)pos.getY() + 0.5D,
                (double)pos.getZ() + 0.5D,
                3.0D, // Vanilla book-opening distance
                false
        );

        // Only spawn particles if a player is nearby
        if (playerEntity == null) {
            return;
        }

        Random random = ((EnchantingTableBlockEntityAccessor) blockEntity).getRandom();

        for(BlockPos providerOffset : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
            // Check if the transmitter (air) block is clear
            if (world.getBlockState(pos.add(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2)).isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {

                BlockPos providerPos = pos.add(providerOffset);

                // Use the client-safe particle check
                if (!EnchantmentLib.shouldSpawnParticles(world, providerPos)) {
                    continue;
                }

                int power = EnchantmentLib.getAgnosticEnchantingPower(world, providerPos);
                if (power <= 0) {
                    continue;
                }

                // Scaled probability check for particle generation
                if (random.nextInt(16) < power) {
                    world.addParticle(ParticleTypes.ENCHANT,
                            (double)pos.getX() + 0.5D,
                            (double)pos.getY() + 2.0D,
                            (double)pos.getZ() + 0.5D,
                            (double)((float)providerOffset.getX() + random.nextFloat()) - 0.5D,
                            (double)((float)providerOffset.getY() - random.nextFloat() - 1.0F),
                            (double)((float)providerOffset.getZ() + random.nextFloat()) - 0.5D
                    );
                }
            }
        }
    }
}