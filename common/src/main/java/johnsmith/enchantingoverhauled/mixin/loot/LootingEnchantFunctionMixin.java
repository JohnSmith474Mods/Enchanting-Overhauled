package johnsmith.enchantingoverhauled.mixin.loot;

import johnsmith.enchantingoverhauled.config.Config;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootingEnchantFunction.class)
public class LootingEnchantFunctionMixin {

    @Final
    @Shadow private int limit;

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void modifyLootingDrops(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
        Entity killer = context.getParamOrNull(LootContextParams.KILLER_ENTITY);
        if (!(killer instanceof LivingEntity livingKiller)) {
            return;
        }

        int level = EnchantmentHelper.getMobLooting(livingKiller);
        if (level <= 0) {
            return;
        }

        if (!Config.TOMES_HAVE_GREATER_ENCHANTMENTS && level > Config.ENCHANTMENT_MAX_LEVEL) {
            level = Config.ENCHANTMENT_MAX_LEVEL;
        }

        float currentLimit = Config.LOOTING_INITIAL_LIMIT;
        float decay = Config.LOOTING_DIMINISHING_RETURNS;
        float limitAccumulator = 0;
        for (int i = 0; i < level; i++) {
            limitAccumulator += currentLimit;
            currentLimit = Math.max(currentLimit - decay, Config.LOOTING_MINIMUM_INCREMENT);
        }

        int maxExtraDrops = (int) Math.floor(limitAccumulator);

        if (maxExtraDrops > 0) {
            int extraDrops = context.getRandom().nextInt(maxExtraDrops + 1);
            stack.grow(extraDrops);
        }

        if (this.limit > 0) {
            stack.limitSize(this.limit);
        }

        cir.setReturnValue(stack);
    }
}