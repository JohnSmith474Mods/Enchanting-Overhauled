package johnsmith.enchantingoverhauled.mixin.block.entity;

import johnsmith.enchantingoverhauled.accessor.TomeStorageAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EnchantingTableBlockEntity.class)
public abstract class EnchantingTableBlockEntityMixin extends BlockEntity implements TomeStorageAccessor {
    // Required Constructor
    public EnchantingTableBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // Accessor Implementation
    @Unique
    private ItemStack enchanting_overhauled$tomeStack = ItemStack.EMPTY;

    @Override
    public ItemStack enchanting_overhauled$getTomeStack() {
        return this.enchanting_overhauled$tomeStack;
    }

    @Override
    public void enchanting_overhauled$setTomeStack(ItemStack stack) {
        this.enchanting_overhauled$tomeStack = stack;
        this.setChanged();
    }
}
