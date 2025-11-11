package johnsmith.mixin.screen;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.BiConsumer;

import johnsmith.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.*;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin extends ForgingScreenHandlerMixin {

    // Override to allow taking output regardless of level cost.
    @Inject(method = "canTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Z)Z",
            at = @At("HEAD"),
            cancellable = true)
    public void canTakeOutput(PlayerEntity player, boolean present,  CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        return;
    }

    // Override to remove level cost.
    @Inject(method = "getLevelCost()I",
            at = @At("HEAD"),
            cancellable = true)
    public void getLevelCost(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
        return;
    }

    // Remove experience level deduction on output take.
    @Redirect(method = "onTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
                  at = @At(value = "INVOKE",
                          target = "Lnet/minecraft/entity/player/PlayerEntity;addExperienceLevels(I)V"))
    protected void noRepairCostInject(PlayerEntity player, int x) {
    }

    // Modifiy amount of eligible repair items required for item repair.
    // Default is 4.
    // Using modify constant is not advised here, but I am too lazy to find
    // another way.
    @ModifyConstant(method = "updateResult()V", constant = @Constant(intValue = 4))
    private int maxItemCost(int value) {
        return Config.ANVIL_MAX_ITEM_COST;
    }

    // Modify the repair bonus applied when repairing items.
    // Default is 12.
    // Using modify constant is not advised here, but I am too lazy to find
    // another way.
    @ModifyConstant(method = "updateResult()V", constant = @Constant(intValue = 12))
    private int repairBonus(int value) {
        return Config.ANVIL_REPAIR_BONUS;
    }

    // Modify anvil break chance when taking output.
    // Default is 0.12F (12%).
    @Redirect(method = "onTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
                  at = @At(value = "INVOKE",
                          target = "Lnet/minecraft/screen/ScreenHandlerContext;run(Ljava/util/function/BiConsumer;)V"))
    private void modifyAnvilBreak(ScreenHandlerContext context, BiConsumer<World, BlockPos> originalLambda,
                                  PlayerEntity player, ItemStack stack) {

        // This is a new lambda that reimplements the original,
        // but uses the ANVIL_BREAK_CHANCE variable.
        context.run((world, pos) -> {
            BlockState blockState = world.getBlockState(pos);

            // Check anvil break chance
            if (!player.getAbilities().creativeMode &&
                    blockState.isIn(BlockTags.ANVIL) &&
                    player.getRandom().nextFloat() < Config.ANVIL_BREAK_CHANCE.floatValue()) {

                // Anvil breaks
                BlockState blockState2 = AnvilBlock.getLandingState(blockState);
                if (blockState2 == null) {
                    world.removeBlock(pos, false);
                    world.syncWorldEvent(WorldEvents.ANVIL_DESTROYED, pos, 0);
                } else {
                    world.setBlockState(pos, blockState2, 2);
                    world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);
                }
            } else {
                // Anvil does not break
                world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);
            }
        });
    }

    // Disallow using enchanted books in anvil combinations.
    @ModifyVariable(method = "updateResult()V",
                        at = @At("STORE"),
                   ordinal = 2)
    private ItemStack disallowEnchantedBooks(ItemStack itemStack) {
        if (this.input.getStack(1).getItem() instanceof EnchantedBookItem)
            return ItemStack.EMPTY;
        else
            return itemStack;
    }

    // Disallow combining enchantments from the second input item.
    @ModifyVariable(method = "updateResult()V",
                        at = @At("STORE"),
                   ordinal = 0)
    private Iterator disallowEnchantmentCombination(Iterator itemStack) {
        return Collections.emptyIterator();
    }
}
