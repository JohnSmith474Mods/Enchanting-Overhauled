package johnsmith.mixin.client.gui.screen.ingame;

import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    @ModifyArgs(method = "drawForeground(Lnet/minecraft/client/gui/DrawContext;II)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I",
                    ordinal = 1)) // Move the inventory title text down.
    private void modifyForegroundDrawHeight(Args args) {
        if ((Object) this instanceof EnchantmentScreen) {
            int originalY = args.get(3);
            args.set(3, originalY + 29);
        }
    }
}
