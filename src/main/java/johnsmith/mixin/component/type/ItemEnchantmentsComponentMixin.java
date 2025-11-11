package johnsmith.mixin.component.type;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

@Mixin(ItemEnchantmentsComponent.class)
public abstract class ItemEnchantmentsComponentMixin {

    @Shadow @Final
    Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchantments;

    @Shadow @Final
    boolean showInTooltip;

    @Unique
    private static final Text INDENT = Text.literal("  ");

    @Unique
    private static final int MAX_LINE_LENGTH = 40;

    /**
     * Helper method to add the indented and wrapped enchantment's description text.
     */
    @Unique
    private void addEnchantmentDescription(Enchantment enchantment, Consumer<Text> tooltip) {
        String descriptionKey = enchantment.getTranslationKey() + ".desc";
        // Get the translated string from the client's language map
        String descriptionString = Text.translatable(descriptionKey).getString();

        // Do nothing if the description is missing or empty
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

            tooltip.accept(INDENT.copy().append(Text.literal(line)).formatted(Formatting.GRAY));
        }

        // Add the final remaining part of the string
        if (!remainingString.isEmpty()) {
            tooltip.accept(INDENT.copy().append(Text.literal(remainingString)).formatted(Formatting.GRAY));
        }
    }

    /**
     * Helper method to add a newline spacer *before* an enchantment,
     * skipping the very first one.
     */
    @Unique
    private void addNewlineBefore(Consumer<Text> tooltip, boolean isFirst) {
        if (!isFirst) {
            tooltip.accept(Text.empty());
        }
    }

    /**
     * A copy of the private static getTooltipOrderList method from the original class.
     */
    @Unique
    private static <T> RegistryEntryList<T> getTooltipOrderList(@Nullable RegistryWrapper.WrapperLookup registryLookup, RegistryKey<Registry<T>> registryRef, TagKey<T> tooltipOrderTag) {
        if (registryLookup != null) {
            Optional<RegistryEntryList.Named<T>> optional = registryLookup.getWrapperOrThrow(registryRef).getOptional(tooltipOrderTag);
            if (optional.isPresent()) {
                return (RegistryEntryList)optional.get();
            }
        }
        return RegistryEntryList.of(new RegistryEntry[0]);
    }

    /**
     * Overwrites the original appendTooltip method entirely.
     */
    @Inject(
            method = "appendTooltip(Lnet/minecraft/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/client/item/TooltipType;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void overwriteAppendTooltip(
            Item.TooltipContext context,
            Consumer<Text> tooltip,
            TooltipType type,
            CallbackInfo ci
    ) {
        if (this.showInTooltip) {
            RegistryWrapper.WrapperLookup wrapperLookup = context.getRegistryLookup();
            RegistryEntryList<Enchantment> registryEntryList = getTooltipOrderList(wrapperLookup, RegistryKeys.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);

            boolean isFirst = true;

            // --- Phase 1: The "Ordered" Loop ---
            for(RegistryEntry<Enchantment> registryEntry : registryEntryList) {
                int i = this.enchantments.getInt(registryEntry);
                if (i > 0) {
                    // this.addNewlineBefore(tooltip, isFirst);
                    isFirst = false; // Flag is updated after the first item is found

                    Enchantment enchantment = registryEntry.value();
                    tooltip.accept(enchantment.getName(i));
                    this.addEnchantmentDescription(enchantment, tooltip);
                }
            }

            // --- Phase 2: The "Unordered" Loop ---
            ObjectIterator<Object2IntMap.Entry<RegistryEntry<Enchantment>>> var9 = this.enchantments.object2IntEntrySet().iterator();

            while(var9.hasNext()) {
                Object2IntMap.Entry<RegistryEntry<Enchantment>> entry = var9.next();
                RegistryEntry<Enchantment> registryEntry2 = entry.getKey();

                // Check if it was already added in Phase 1
                if (!registryEntryList.contains(registryEntry2)) {
                    // this.addNewlineBefore(tooltip, isFirst);
                    isFirst = false; // Flag is updated after the first item is found

                    Enchantment enchantment = registryEntry2.value();
                    tooltip.accept(enchantment.getName(entry.getIntValue()));
                    this.addEnchantmentDescription(enchantment, tooltip);
                }
            }
        }

        ci.cancel(); // Cancel the original method
    }
}