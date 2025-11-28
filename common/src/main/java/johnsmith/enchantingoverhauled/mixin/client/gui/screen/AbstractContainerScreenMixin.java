package johnsmith.enchantingoverhauled.mixin.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @ModifyArgs(method = "renderLabels",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",
                    ordinal = 1)) // Move the inventory title text down.
    private void modifyForegroundDrawHeight(Args args) {
        if ((Object) this instanceof EnchantmentScreen) {
            int originalY = args.get(3);
            args.set(3, originalY + 29);
        }
    }
}