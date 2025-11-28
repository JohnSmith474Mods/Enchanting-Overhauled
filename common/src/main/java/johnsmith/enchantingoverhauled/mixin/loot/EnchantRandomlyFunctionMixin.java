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
import java.util.stream.Collectors;

@Mixin(EnchantRandomlyFunction.class)
public class EnchantRandomlyFunctionMixin {

    // Verify this field name in your mappings (Mojmap usually uses 'options' or 'enchantments')
    @Shadow @Final
    private Optional<HolderSet<Enchantment>> options;

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void applyTomeEnchantments(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {

        if (!stack.is(Services.PLATFORM.getEnchantedTome())) {
            return;
        }

        cir.cancel();

        // 2. Update Candidate Selection
        List<Holder<Enchantment>> candidates = this.options
                .map(HolderSet::stream)
                .orElseGet(() -> context.getLevel().registryAccess()
                        // FIX: Use lookupOrThrow and listElements() for 1.21.4 registry access
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .listElements()
                        .map(h -> (Holder<Enchantment>) h))
                .collect(Collectors.toList());

        // 3. Delegate to your library logic
        ItemStack result = EnchantmentLib.applyTomeEnchantments(stack, candidates, context.getRandom());

        cir.setReturnValue(result);
    }
}