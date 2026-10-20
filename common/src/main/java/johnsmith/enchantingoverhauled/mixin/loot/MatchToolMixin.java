package johnsmith.enchantingoverhauled.mixin.loot;

import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponents;
import johnsmith.enchantingoverhauled.api.enchantment.effect.SilkTouchEffect;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(MatchTool.class)
public class MatchToolMixin {

    @Shadow @Final private Optional<ItemPredicate> predicate;

    @Inject(method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At("RETURN"), cancellable = true)
    private void applySilkTouchProbability(LootContext context, CallbackInfoReturnable<Boolean> cir) {
        // 1. If vanilla check failed, or we have no predicate, do nothing.
        if (!cir.getReturnValue() || this.predicate.isEmpty()) return;

        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null || tool.isEmpty()) return;

        // 2. Check for Silk Touch Effect
        var effectOpt = this.enchanting_overhauled$getSilkTouchEffect(tool);
        if (effectOpt.isEmpty()) return;

        SilkTouchEffect effect = effectOpt.get().getKey();
        int level = effectOpt.get().getValue();

        // 3. Verify this predicate actually requires Silk Touch to pass
        if (!enchanting_overhauled$doesPredicateRequireSilkTouch(this.predicate.get(), context)) return;

        // 4. Calculate Probability
        float chance = effect.chance().calculate(level);

        // 5. Roll RNG
        if (context.getRandom().nextFloat() > chance) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Scans the tool for any enchantment that provides the custom Silk Touch effect.
     * Returns the effect instance and the level of the enchantment providing it.
     */
    @Unique
    @SuppressWarnings("unchecked")
    private Optional<Map.Entry<SilkTouchEffect, Integer>> enchanting_overhauled$getSilkTouchEffect(ItemStack stack) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) return Optional.empty();

        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            int level = entry.getIntValue();

            SilkTouchEffect effect = holder.value().effects().get((DataComponentType<SilkTouchEffect>) EnchantmentEffectComponents.SILK_TOUCH.get());
            if (effect != null) {
                return Optional.of(Map.entry(effect, level));
            }
        }
        return Optional.empty();
    }

    @Unique
    private boolean enchanting_overhauled$doesPredicateRequireSilkTouch(ItemPredicate predicate, LootContext context) {
        // Create a dummy tool with NO enchantments
        ItemStack dummy = context.getParamOrNull(LootContextParams.TOOL).copy();
        EnchantmentHelper.setEnchantments(dummy, ItemEnchantments.EMPTY);

        // If the predicate fails on the dummy tool, it implies the predicate REQUIRED the enchantments to pass.
        return !predicate.test(dummy);
    }
}