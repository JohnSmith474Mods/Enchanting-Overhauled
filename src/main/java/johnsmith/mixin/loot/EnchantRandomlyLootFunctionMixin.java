package johnsmith.mixin.loot;

import com.mojang.logging.LogUtils;
import johnsmith.item.ItemRegistry;
import net.minecraft.component.DataComponentTypes; // <-- IMPORT
import net.minecraft.component.type.ItemEnchantmentsComponent; // <-- IMPORT
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(EnchantRandomlyLootFunction.class)
public class EnchantRandomlyLootFunctionMixin {

    // Shadow the private 'enchantments' field
    @Shadow @Final
    private Optional<RegistryEntryList<Enchantment>> enchantments;

    // Create our own logger instance
    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();

    @Unique
    private static final int TOME_ENCHANTMENT_COUNT = 3;

    /**
     * Injects at the head of the process method to apply special logic
     * for Enchanted Tomes.
     */
    @Inject(method = "process(Lnet/minecraft/item/ItemStack;Lnet/minecraft/loot/context/LootContext;)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true)
    private void applyTomeEnchantments(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {

        // If the item is not an Enchanted Tome, do nothing and let the vanilla logic run.
        if (!stack.isOf(ItemRegistry.ENCHANTED_TOME)) {
            return;
        }

        // --- Custom Logic for Enchanted Tome ---

        // 1. Cancel the original method
        cir.cancel();

        Random random = context.getRandom();

        // 2. Get the list of all possible enchantments
        List<RegistryEntry<Enchantment>> enchantmentList;

        if (this.enchantments.isPresent()) {
            // Use the specific list from the loot function if one was provided
            enchantmentList = this.enchantments.get().stream().toList();
        } else {
            // Otherwise, get all valid enchantments from the registry
            enchantmentList = Registries.ENCHANTMENT.streamEntries()
                    .filter((entry) -> entry.value().isEnabled(context.getWorld().getEnabledFeatures()))
                    .collect(Collectors.toList());
        }

        // 3. Shuffle the list using Minecraft's Util class
        List<RegistryEntry<Enchantment>> mutableList = new ArrayList<>(enchantmentList);
        Util.shuffle(mutableList, random);

        // --- THIS IS THE FIX ---
        // 4. Get a builder for the STORED_ENCHANTMENTS component
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(
                stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
        );

        // 5. Apply the first 3 enchantments
        int count = 0;
        for (RegistryEntry<Enchantment> entry : mutableList) {
            if (count >= TOME_ENCHANTMENT_COUNT) {
                break;
            }

            Enchantment enchantment = entry.value();

            // 6. Apply custom level logic
            int maxLevel = enchantment.getMaxLevel();
            int newLevel = (maxLevel == 1) ? 1 : maxLevel + 1;

            // 7. Add the enchantment to the builder
            builder.add(entry.value(), newLevel);
            count++;
        }

        // 8. Apply the built component back to the stack
        stack.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
        // --- END FIX ---

        if (count == 0) {
            LOGGER.warn("Couldn't find any compatible enchantments for {}", stack);
        }

        // 9. Return the modified stack
        cir.setReturnValue(stack);
    }
}