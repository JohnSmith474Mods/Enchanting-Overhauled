package johnsmith.mixin.item;

import net.minecraft.client.item.TooltipType;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TooltipAppender;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {

    @Shadow
    public abstract Item getItem();
    @Unique
    private static final Text APPLIED_HEADER = Text.translatable("item.enchanting_overhauled.applied_enchantments");
    @Unique
    private static final Text STORED_HEADER = Text.translatable("item.enchanting_overhauled.stored_enchantments");


    @Inject(method = "isEnchantable()Z", at = @At("HEAD"), cancellable = true)
    private void modifyIsEnchantable(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;

        if (!this.getItem().isEnchantable(self)) {
            cir.setReturnValue(false);
            return;
        }

        Object itemEnchantmentsComponent = this.get(DataComponentTypes.ENCHANTMENTS);

        cir.setReturnValue(itemEnchantmentsComponent != null);
        return;
    }

    /**
     * Injects at the head of the private appendTooltip method to add headers
     * with color based on enchantment count.
     */
    @Inject(method = "appendTooltip(Lnet/minecraft/component/DataComponentType;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/client/item/TooltipType;)V",
            at = @At("HEAD"))
    private <T extends TooltipAppender> void injectEnchantmentHeaders(
            DataComponentType<T> componentType,
            Item.TooltipContext context,
            Consumer<Text> textConsumer,
            TooltipType type,
            CallbackInfo ci
    ) {
        ItemEnchantmentsComponent enchantments = null;
        Text baseHeader = null;

        if (componentType == DataComponentTypes.ENCHANTMENTS) {
            enchantments = this.get(DataComponentTypes.ENCHANTMENTS);
            baseHeader = APPLIED_HEADER;
        } else if (componentType == DataComponentTypes.STORED_ENCHANTMENTS) {
            enchantments = this.get(DataComponentTypes.STORED_ENCHANTMENTS);
            baseHeader = STORED_HEADER;
        }

        // Check if we found a relevant component
        if (enchantments != null) {
            int count = enchantments.getSize();
            if (count > 0) {
                Formatting color = getColor(count);
                // Add the header with the new color
                textConsumer.accept(baseHeader.copy().formatted(color));
            }
        }
    }

    @Unique
    private static @NotNull Formatting getColor(int count) {
        Formatting color;
        if (count == 1) {
            color = Formatting.YELLOW;
        } else if (count == 2) {
            color = Formatting.AQUA;
        } else { // 3 or more
            color = Formatting.LIGHT_PURPLE;
        }
        return color;
    }
}