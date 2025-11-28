package johnsmith.enchantingoverhauled.mixin.menu;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.BiConsumer;

import johnsmith.enchantingoverhauled.config.Config;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin extends ItemCombinerMenuMixin {

    // Override to allow taking output regardless of level cost.
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void mayPickup(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    // Override to remove level cost.
    @Inject(method = "getCost", at = @At("HEAD"), cancellable = true)
    public void getCost(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }

    // Remove experience level deduction on output take.
    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"))
    protected void noRepairCostInject(Player player, int levels) {
        // No-op
    }

    // Modify amount of eligible repair items required for item repair.
    // Default is 4.
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 4))
    private int maxItemCost(int value) {
        return Config.BOUNDED_ANVIL_MAX_ITEM_COST.get();
    }

    // Modify the repair bonus applied when repairing items.
    // Default is 12.
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 12))
    private int repairBonus(int value) {
        return Config.BOUNDED_ANVIL_REPAIR_BONUS.get();
    }

    // Modify anvil break chance when taking output.
    // Default is 0.12F (12%).
    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"))
    private void modifyAnvilBreak(ContainerLevelAccess context, BiConsumer<Level, BlockPos> originalLambda, Player player, ItemStack stack) {

        // reimplements the original lambda logic using Config.ANVIL_BREAK_CHANCE
        context.execute((level, pos) -> {
            BlockState blockState = level.getBlockState(pos);

            // Check anvil break chance
            if (!player.getAbilities().instabuild &&
                    blockState.is(BlockTags.ANVIL) &&
                    player.getRandom().nextFloat() < Config.BOUNDED_ANVIL_BREAK_CHANCE.get().floatValue()) {

                // Anvil breaks (damage returns the next state, e.g. chipped -> damaged)
                BlockState blockState2 = AnvilBlock.damage(blockState);

                if (blockState2 == null) {
                    level.removeBlock(pos, false);
                    level.levelEvent(LevelEvent.SOUND_ANVIL_BROKEN, pos, 0);
                } else {
                    level.setBlock(pos, blockState2, 2);
                    level.levelEvent(LevelEvent.SOUND_ANVIL_USED, pos, 0);
                }
            } else {
                // Anvil does not break
                level.levelEvent(LevelEvent.SOUND_ANVIL_USED, pos, 0);
            }
        });
    }

    // Disallow using enchanted books in anvil combinations.
    @ModifyVariable(method = "createResult", at = @At("STORE"), ordinal = 2)
    private ItemStack disallowEnchantedBooks(ItemStack itemStack) {
        if (this.inputSlots.getItem(1).has(DataComponents.STORED_ENCHANTMENTS)) {
            return ItemStack.EMPTY;
        } else {
            return itemStack;
        }
    }

    // Disallow combining enchantments from the second input item.
    @ModifyVariable(method = "createResult", at = @At("STORE"), ordinal = 0)
    private Iterator<?> disallowEnchantmentCombination(Iterator<?> itemStack) {
        return Collections.emptyIterator();
    }
}