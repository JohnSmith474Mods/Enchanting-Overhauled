package johnsmith.enchantingoverhauled.mixin.loot;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.platform.Services;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
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

@Mixin(EnchantRandomlyFunction.class)
public class EnchantRandomlyFunctionMixin {

    // Shadow the private 'options' field (Mojang mapping for 'enchantments')
    @Shadow @Final
    private Optional<HolderSet<Enchantment>> enchantments;

    @Unique
    private static final int TOME_ENCHANTMENT_COUNT = 3;

    /**
     * Injects at the head of the run method to apply special logic
     * for Enchanted Tomes.
     */
    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void applyTomeEnchantments(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {

        // If the item is not an Enchanted Tome, do nothing and let the vanilla logic run.
        if (!stack.is(Services.PLATFORM.getEnchantedTome())) {
            return;
        }

        // 1. Cancel the original method
        cir.cancel();

        // 2. Resolve the list of candidates based on the Loot Function's specific filter
        // This respects datapack-defined filters in the loot table
        List<Enchantment> candidates = this.enchantments
                .map(holders -> holders.stream().map(Holder::value).toList())
                .orElseGet(() -> BuiltInRegistries.ENCHANTMENT.holders()
                        .filter((entry) -> entry.value().isEnabled(context.getLevel().enabledFeatures()))
                        .map(Holder::value)
                        .collect(Collectors.toList()));

        // 3. Delegate to Library logic
        ItemStack result = EnchantmentLib.applyTomeEnchantments(stack, candidates, context.getRandom());

        // 4. Return the modified stack
        cir.setReturnValue(result);
    }
}