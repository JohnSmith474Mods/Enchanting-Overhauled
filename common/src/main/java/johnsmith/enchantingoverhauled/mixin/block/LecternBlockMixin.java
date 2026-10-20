package johnsmith.enchantingoverhauled.mixin.block;

import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.menu.scribe.ScribeMenu;
import johnsmith.enchantingoverhauled.platform.Services;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternBlock.class)
public class LecternBlockMixin {

    @Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
    private void enchanting_overhauled$interceptEnchantedBookScreen(Level level, BlockPos pos, Player player, CallbackInfo ci) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof LecternBlockEntity lecternBlockEntity) {
            ItemStack book = lecternBlockEntity.getBook();
            if (book.is(net.minecraft.world.item.Items.ENCHANTED_BOOK) || book.is(Items.ENCHANTED_TOME.get())) {
                if (!level.isClientSide) {
                    player.openMenu(new SimpleMenuProvider(
                            (containerId, playerInventory, playerEntity) -> new ScribeMenu(
                                    containerId,
                                    playerInventory,
                                    ContainerLevelAccess.create(level, pos)
                            ),
                            Component.translatable("container.enchanting_overhauled.scribe")
                    ));
                }
                ci.cancel();
            }
        }
    }
}