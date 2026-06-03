package johnsmith.enchantingoverhauled.mixin.component.type;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import johnsmith.enchantingoverhauled.config.Config;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ItemEnchantments.class)
public abstract class ItemEnchantmentsMixin {

    @Shadow @Final
    Object2IntOpenHashMap<Holder<Enchantment>> enchantments;

    @Unique
    private static final Component enchanting_Overhauled$INDENT = Component.literal("  ");

    @Unique
    private static final int MAX_LINE_LENGTH = 40;

    @Unique
    private void enchanting_Overhauled$addEnchantmentDescription(Holder<Enchantment> enchantmentHolder, Consumer<Component> tooltip) {
        if (!Config.BINARY_SHOW_TOOLTIP_ENCHANTMENT_DESCRIPTIONS.get() || !Config.BINARY_ENABLE_ENCHANTMENT_TOOLTIP_MODIFICATIONS.get()) {
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

            tooltip.accept(enchanting_Overhauled$INDENT.copy().append(Component.literal(line)).withStyle(descStyle));
        }

        if (!remainingString.isEmpty()) {
            tooltip.accept(enchanting_Overhauled$INDENT.copy().append(Component.literal(remainingString)).withStyle(descStyle));
        }
    }

    @Unique
    private static <T> HolderSet<T> enchanting_Overhauled$getTooltipOrderList(@Nullable HolderLookup.Provider registryLookup, ResourceKey<Registry<T>> registryRef, TagKey<T> tooltipOrderTag) {
        if (registryLookup != null) {
            Optional<HolderSet.Named<T>> optional = registryLookup.lookupOrThrow(registryRef).get(tooltipOrderTag);
            if (optional.isPresent()) {
                return optional.get();
            }
        }
        return HolderSet.direct();
    }

    @WrapOperation(
            method = "addToTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    ordinal = 0
            )
    )
    private void enchanting_Overhauled$wrapTooltipLoop1(
            Consumer<Component> tooltipAdder,
            Object componentObj, // The Component being added (Name + Level)
            Operation<Void> original,
            @Local Holder<Enchantment> holder // Captured Local Variable!
    ) {
        // 1. Run original logic (Add the Enchantment Name)
        original.call(tooltipAdder, componentObj);

        // 2. Run our logic (Add the Description immediately after)
        this.enchanting_Overhauled$addEnchantmentDescription(holder, tooltipAdder);
    }

    @WrapOperation(
            method = "addToTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    ordinal = 1
            )
    )
    private void enchanting_Overhauled$wrapTooltipLoop2(
            Consumer<Component> tooltipAdder,
            Object componentObj,
            Operation<Void> original,
            @Local Object2IntMap.Entry<Holder<Enchantment>> entry // Captured Map Entry!
    ) {
        // 1. Run original logic (Add the Enchantment Name)
        original.call(tooltipAdder, componentObj);

        // 2. Run our logic (Extract Key -> Add Description)
        this.enchanting_Overhauled$addEnchantmentDescription(entry.getKey(), tooltipAdder);
    }
}