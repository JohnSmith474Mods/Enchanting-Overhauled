package johnsmith.enchantingoverhauled.mixin.loot;

import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ApplyBonusCount.class)
public class ApplyBonusCountMixin {

    @Shadow @Final private Holder<Enchantment> enchantment;

    /**
     * Overrides the run method to apply custom Fortune logic.
     * This targets the "ore_drops" behavior (multiplier) specifically or generally applies
     * to all bonus counts if they use the Fortune enchantment.
     */
    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void modifyFortuneDrops(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
        if (this.enchantment.value() != Enchantments.FORTUNE) {
            return;
        }

        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null) return;

        int level = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment.value(), tool);
        if (level <= 0) return;

        if (!Config.TOMES_HAVE_GREATER_ENCHANTMENTS && level > Config.ENCHANTMENT_MAX_LEVEL) {
            level = Config.ENCHANTMENT_MAX_LEVEL;
        }

        float currentLimit = Config.FORTUNE_INITIAL_LIMIT;
        float decay = Config.FORTUNE_DIMINISHING_RETURNS;
        float limitAccumulator = 0;

        for (int i = 0; i < level; i++) {
            limitAccumulator += currentLimit;
            currentLimit = Math.max(currentLimit - decay, Config.FORTUNE_MINIMUM_INCREMENT);
        }

        RandomSource random = context.getRandom();

        int maxBonus = (int) Math.floor(limitAccumulator);
        int multiplier = Math.max(1, random.nextInt(maxBonus + 2));

        stack.setCount(stack.getCount() * multiplier);

        cir.setReturnValue(stack);
    }
}