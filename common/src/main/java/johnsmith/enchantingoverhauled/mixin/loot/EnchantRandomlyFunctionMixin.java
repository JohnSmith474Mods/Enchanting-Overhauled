package johnsmith.enchantingoverhauled.mixin.loot;

import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.platform.Services;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mixin(EnchantRandomlyFunction.class)
public class EnchantRandomlyFunctionMixin {

    // 1. Rename shadowed field to match vanilla 1.21 'options'
    @Shadow @Final
    private Optional<HolderSet<Enchantment>> options;

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void applyTomeEnchantments(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {

        if (!stack.is(Services.PLATFORM.getEnchantedTome())) {
            return;
        }

        cir.cancel();

        // 2. Update Candidate Selection
        // Use the 'options' field if present, otherwise fetch all enchantments from the level's registry access.
        // In 1.21, enchantments are dynamic, so avoid BuiltInRegistries.
        List<Holder<Enchantment>> candidates = this.options
                .map(HolderSet::stream)
                .orElseGet(() -> context.getLevel().registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .holders()
                        .map(h -> (Holder<Enchantment>) h))
                .collect(Collectors.toList());

        // 3. Delegate to your library logic
        ItemStack result = EnchantmentLib.applyTomeEnchantments(stack, candidates, context.getRandom());

        cir.setReturnValue(result);
    }
}