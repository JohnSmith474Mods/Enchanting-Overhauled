package johnsmith.mixin.screen;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ForgingScreenHandler;

@Mixin(ForgingScreenHandler.class)
public class ForgingScreenHandlerMixin {
    // Allow extending classes to access input inventory.
    @Shadow
    @Final
    protected Inventory input;
}
