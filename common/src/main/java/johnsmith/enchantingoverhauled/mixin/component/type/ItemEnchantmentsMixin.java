package johnsmith.enchantingoverhauled.mixin.component.type;

import com.llamalad7.mixinextras.sugar.Local;
import johnsmith.enchantingoverhauled.config.Config;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemEnchantments.class)
@Debug(export = true)
public abstract class ItemEnchantmentsMixin {
    @Unique
    private static final Component INDENT = Component.literal("  ");

    @Unique
    private static final int MAX_LINE_LENGTH = 40;

    @Unique
    private void enchanting_Overhauled$addEnchantmentDescription(Holder<Enchantment> enchantmentHolder, Consumer<Component> tooltip) {
        if (!Config.BINARY_ACCESSIBILITY_SHOW_ENCHANTMENT_DESCRIPTIONS.get()) {
            return;
        }

        var keyOpt = enchantmentHolder.unwrapKey();
        if (keyOpt.isEmpty()) return;

        String descriptionKey = Util.makeDescriptionId("enchantment", keyOpt.get().location()) + ".desc";

        String descriptionString = Component.translatable(descriptionKey).getString();

        // If translation fails (key == result), abort
        if (descriptionString.isEmpty() || descriptionString.equals(descriptionKey)) {
            return;
        }

        Style descStyle = Style.EMPTY.withColor(Config.BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR.get());

        String remainingString = descriptionString;

        while (remainingString.length() > MAX_LINE_LENGTH) {
            String line;
            int wrapAt = remainingString.lastIndexOf(' ', MAX_LINE_LENGTH);

            if (wrapAt <= 0) {
                line = remainingString.substring(0, MAX_LINE_LENGTH);
                remainingString = remainingString.substring(MAX_LINE_LENGTH);
            } else {
                line = remainingString.substring(0, wrapAt);
                remainingString = remainingString.substring(wrapAt + 1);
            }

            tooltip.accept(INDENT.copy().append(Component.literal(line)).withStyle(descStyle));
        }

        if (!remainingString.isEmpty()) {
            tooltip.accept(INDENT.copy().append(Component.literal(remainingString)).withStyle(descStyle));
        }
    }

    @Inject(method = "addToTooltip", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void overwriteAddToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag, CallbackInfo ci, @Local Holder<Enchantment> enchantment) {
        this.enchanting_Overhauled$addEnchantmentDescription(enchantment, tooltip);
    }
}