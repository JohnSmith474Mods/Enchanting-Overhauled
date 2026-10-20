package johnsmith.enchantingoverhauled.mixin.loot;

import johnsmith.enchantingoverhauled.item.EnchantedTomeItem;
import johnsmith.enchantingoverhauled.item.Items;

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

    @Shadow @Final
    private Optional<HolderSet<Enchantment>> options;

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void applyTomeEnchantments(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {

        if (!stack.is(Items.ENCHANTED_TOME.get())) {
            return;
        }

        cir.cancel();

        List<Holder<Enchantment>> candidates = this.options
                .map(HolderSet::stream)
                .orElseGet(() -> context.getLevel().registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .holders()
                        .map(h -> h))
                .collect(Collectors.toList());

        ItemStack result = EnchantedTomeItem.applyTomeEnchantments(stack, candidates, context.getRandom());

        cir.setReturnValue(result);
    }
}