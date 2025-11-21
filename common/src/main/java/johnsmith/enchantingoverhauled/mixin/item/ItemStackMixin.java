package johnsmith.enchantingoverhauled.mixin.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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
public abstract class ItemStackMixin implements DataComponentHolder {

    @Shadow
    public abstract Item getItem();

    @Unique
    private static final Component APPLIED_HEADER = Component.translatable("item.enchanting_overhauled.applied_enchantments");
    @Unique
    private static final Component STORED_HEADER = Component.translatable("item.enchanting_overhauled.stored_enchantments");

    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void modifyIsEnchantable(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;

        if (!this.getItem().isEnchantable(self)) {
            cir.setReturnValue(false);
            return;
        }

        // Mapped: DataComponentTypes.ENCHANTMENTS -> DataComponents.ENCHANTMENTS
        Object itemEnchantmentsComponent = this.get(DataComponents.ENCHANTMENTS);

        cir.setReturnValue(itemEnchantmentsComponent != null);
    }

    /**
     * Injects at the head of the private addToTooltip method to add headers
     * with color based on enchantment count.
     * Mapped: appendTooltip -> addToTooltip
     */
    @Inject(method = "addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
            at = @At("HEAD"))
    private <T extends TooltipProvider> void injectEnchantmentHeaders(
            DataComponentType<T> componentType,
            Item.TooltipContext context,
            Consumer<Component> textConsumer,
            TooltipFlag tooltipFlag,
            CallbackInfo ci
    ) {
        ItemEnchantments enchantments = null;
        Component baseHeader = null;

        if (componentType == DataComponents.ENCHANTMENTS) {
            enchantments = this.get(DataComponents.ENCHANTMENTS);
            baseHeader = APPLIED_HEADER;
        } else if (componentType == DataComponents.STORED_ENCHANTMENTS) {
            enchantments = this.get(DataComponents.STORED_ENCHANTMENTS);
            baseHeader = STORED_HEADER;
        }

        // Check if we found a relevant component
        if (enchantments != null) {
            int count = enchantments.size(); // Mapped: getSize() -> size()
            if (count > 0) {
                ChatFormatting color = getColor(count);
                // Add the header with the new color
                // Mapped: formatted() -> withStyle()
                textConsumer.accept(baseHeader.copy().withStyle(color));
            }
        }
    }

    @Unique
    private static @NotNull ChatFormatting getColor(int count) {
        ChatFormatting color;
        if (count == 1) {
            color = ChatFormatting.YELLOW;
        } else if (count == 2) {
            color = ChatFormatting.AQUA;
        } else { // 3 or more
            color = ChatFormatting.LIGHT_PURPLE;
        }
        return color;
    }
}