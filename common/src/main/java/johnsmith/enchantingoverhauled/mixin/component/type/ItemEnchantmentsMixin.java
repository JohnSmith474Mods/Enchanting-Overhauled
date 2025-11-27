package johnsmith.enchantingoverhauled.mixin.component.type;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import johnsmith.enchantingoverhauled.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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

    /**
     * Helper method to add the indented and wrapped enchantment's description text.
     */
    @Unique
    private void addEnchantmentDescription(Enchantment enchantment, Consumer<Component> tooltip) {
        if (!Config.SHOW_ENCHANTMENT_DESCRIPTIONS) return;

        String descriptionKey = enchantment.getDescriptionId() + ".desc";
        // Get the translated string
        String descriptionString = Component.translatable(descriptionKey).getString();

        // Do nothing if the description is missing (matches key) or empty
        if (descriptionString.isEmpty() || descriptionString.equals(descriptionKey)) {
            return;
        }

        String remainingString = descriptionString;

        // Loop while the remaining string is longer than the max length
        while (remainingString.length() > MAX_LINE_LENGTH) {
            String line;
            // Find the last space within the first 40 characters
            int wrapAt = remainingString.lastIndexOf(' ', MAX_LINE_LENGTH);

            if (wrapAt <= 0) {
                // No space found, or space is at index 0. Hard wrap.
                line = remainingString.substring(0, MAX_LINE_LENGTH);
                remainingString = remainingString.substring(MAX_LINE_LENGTH);
            } else {
                // Space found. Wrap at the space.
                line = remainingString.substring(0, wrapAt);
                // Remove the line and the space after it
                remainingString = remainingString.substring(wrapAt + 1);
            }

            tooltip.accept(INDENT.copy().append(Component.literal(line)).withColor(Config.ENCHANTMENT_DESCRIPTION_COLOR));
        }

        // Add the final remaining part of the string
        if (!remainingString.isEmpty()) {
            tooltip.accept(INDENT.copy().append(Component.literal(remainingString)).withColor(Config.ENCHANTMENT_DESCRIPTION_COLOR));
        }
    }

    /**
     * Helper method to add a newline spacer *before* an enchantment,
     * skipping the very first one.
     */
    @Unique
    private void addNewlineBefore(Consumer<Component> tooltip, boolean isFirst) {
        if (!isFirst) {
            tooltip.accept(Component.empty());
        }
    }

    /**
     * A copy of the private static getTooltipOrderList method from the original class.
     * Mapped: getTooltipOrderList -> logic recreated using Mojang mappings
     */
    @Unique
    private static <T> HolderSet<T> getTooltipOrderList(@Nullable HolderLookup.Provider registryLookup, ResourceKey<Registry<T>> registryRef, TagKey<T> tooltipOrderTag) {
        if (registryLookup != null) {
            Optional<HolderSet.Named<T>> optional = registryLookup.lookupOrThrow(registryRef).get(tooltipOrderTag);
            if (optional.isPresent()) {
                return optional.get();
            }
        }
        return HolderSet.direct();
    }

    /**
     * Overwrites the original addToTooltip method entirely.
     * Mapped: appendTooltip -> addToTooltip
     */
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
            HolderSet<Enchantment> registryEntryList = getTooltipOrderList(wrapperLookup, Registries.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);

            // boolean isFirst = true; // Unused in current logic logic, kept commented out in source

            // --- Phase 1: The "Ordered" Loop ---
            for(Holder<Enchantment> registryEntry : registryEntryList) {
                int i = this.enchantments.getInt(registryEntry);
                if (i > 0) {
                    // this.addNewlineBefore(tooltip, isFirst);
                    // isFirst = false;

                    Enchantment enchantment = registryEntry.value();
                    tooltip.accept(enchantment.getFullname(i));
                    this.addEnchantmentDescription(enchantment, tooltip);
                }
            }

            // --- Phase 2: The "Unordered" Loop ---

            for (Object2IntMap.Entry<Holder<Enchantment>> entry : this.enchantments.object2IntEntrySet()) {
                Holder<Enchantment> registryEntry2 = entry.getKey();

                // Check if it was already added in Phase 1
                if (!registryEntryList.contains(registryEntry2)) {
                    // this.addNewlineBefore(tooltip, isFirst);
                    // isFirst = false;

                    Enchantment enchantment = registryEntry2.value();
                    tooltip.accept(enchantment.getFullname(entry.getIntValue()));
                    this.addEnchantmentDescription(enchantment, tooltip);
                }
            }
        }

        ci.cancel(); // Cancel the original method
    }
}