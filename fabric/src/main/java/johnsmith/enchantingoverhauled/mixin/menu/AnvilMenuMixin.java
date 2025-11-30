package johnsmith.enchantingoverhauled.mixin.menu;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import johnsmith.enchantingoverhauled.config.Config;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    // Modify anvil break chance when taking output.
    // Default is 0.12 (12%).
    @ModifyExpressionValue(method = "method_24922", at = @At(value = "CONSTANT", args = "floatValue=0.12"))
    private static float modifyAnvilBreak(final float breakChance) {
        return Config.BOUNDED_ANVIL_BREAK_CHANCE.get();
    }
}
