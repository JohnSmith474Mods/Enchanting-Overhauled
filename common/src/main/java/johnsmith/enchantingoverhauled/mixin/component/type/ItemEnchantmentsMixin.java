package johnsmith.enchantingoverhauled.mixin.component.type;

import johnsmith.enchantingoverhauled.config.Config;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ItemEnchantments.class)
public abstract class ItemEnchantmentsMixin {

    @Shadow @Final
    Object2IntOpenHashMap<Holder<Enchantment>> enchantments;

    @Shadow @Final
    boolean showInTooltip;

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

    @Inject(
            method = "addToTooltip",
            at = @At("HEAD"),
            cancellable = true
    )
    private void overwriteAddToTooltip(
            Item.TooltipContext context,
            Consumer<Component> tooltip,
            TooltipFlag flag,
            CallbackInfo ci
    ) {
        if (this.showInTooltip) {
            HolderLookup.Provider wrapperLookup = context.registries();
            HolderSet<Enchantment> registryEntryList = enchanting_Overhauled$getTooltipOrderList(wrapperLookup, Registries.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);

            for(Holder<Enchantment> registryEntry : registryEntryList) {
                int i = this.enchantments.getInt(registryEntry);
                if (i > 0) {
                    // Removed unwrapping .value(), passing Holder directly
                    tooltip.accept(Enchantment.getFullname(registryEntry, i));
                    this.enchanting_Overhauled$addEnchantmentDescription(registryEntry, tooltip);
                }
            }

            for (Object2IntMap.Entry<Holder<Enchantment>> entry : this.enchantments.object2IntEntrySet()) {
                Holder<Enchantment> registryEntry2 = entry.getKey();

                if (!registryEntryList.contains(registryEntry2)) {
                    // Removed unwrapping .value(), passing Holder directly
                    tooltip.accept(Enchantment.getFullname(registryEntry2, entry.getIntValue()));
                    this.enchanting_Overhauled$addEnchantmentDescription(registryEntry2, tooltip);
                }
            }
        }

        ci.cancel();
    }
}