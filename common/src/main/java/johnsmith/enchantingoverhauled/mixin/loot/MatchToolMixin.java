package johnsmith.enchantingoverhauled.mixin.loot;

import johnsmith.enchantingoverhauled.api.enchantment.effect.SilkTouchEffect;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MatchTool.class)
public class MatchToolMixin {

    @Shadow @Final private Optional<ItemPredicate> predicate;

    @Inject(method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At("RETURN"), cancellable = true)
    private void applySilkTouchProbability(LootContext context, CallbackInfoReturnable<Boolean> cir) {
        // 1. If vanilla check failed, or we have no predicate, do nothing.
        if (!cir.getReturnValue() || this.predicate.isEmpty()) return;

        ItemStack tool = context.getOptionalParameter(LootContextParams.TOOL);
        if (tool == null || tool.isEmpty()) return;

        // 2. Check for Silk Touch Effect
        var effectOpt = EnchantmentLib.getSilkTouchEffect(tool, context.getLevel().registryAccess());
        if (effectOpt.isEmpty()) return;

        SilkTouchEffect effect = effectOpt.get().getKey();
        int level = effectOpt.get().getValue(); // <--- Level retrieved here

        // 3. Verify this predicate actually requires Silk Touch to pass
        if (!doesPredicateRequireSilkTouch(this.predicate.get(), context)) return;

        // 4. Calculate Probability
        float chance = effect.chance().calculate(level);

        // 5. Roll RNG
        if (context.getRandom().nextFloat() > chance) {
            cir.setReturnValue(false);
        }
    }

    private boolean doesPredicateRequireSilkTouch(ItemPredicate predicate, LootContext context) {
        // Create a dummy tool with NO enchantments
        ItemStack dummy = context.getOptionalParameter(LootContextParams.TOOL).copy();
        EnchantmentHelper.setEnchantments(dummy, ItemEnchantments.EMPTY);

        // If the predicate fails on the dummy tool, it implies the predicate REQUIRED the enchantments to pass.
        return !predicate.test(dummy);
    }
}