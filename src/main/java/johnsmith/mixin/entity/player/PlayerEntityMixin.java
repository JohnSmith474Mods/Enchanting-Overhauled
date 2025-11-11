package johnsmith.mixin.entity.player;

import johnsmith.config.Config;
import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    // Shadow the experienceLevel field so we can access it
    @Shadow
    public int experienceLevel;

    @Definition(id = "i", local = @Local(type = int.class))
    @Expression("i * i")
    @ModifyExpressionValue(method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int modifyEfficiencyCalculation(int original, @Local(type = int.class) int i) {
        return original * i;
    }

    /**
     * Overrides the calculation for XP needed for the next level.
     * The formula provides stepped, linear progression defined by configuration.
     * <p>
     * The cost to advance from [currentLevel] to [currentLevel + 1] is calculated as:
     * (Config.XP_GROWTH_FACTOR * Step) + Config.XP_GROWTH_Y_OFFSET
     * where Step is based on brackets defined by Config.XP_LEVEL_BRACKET_SIZE.
     */
    @Inject(method = "getNextLevelExperience()I", at = @At("HEAD"), cancellable = true)
    private void onGetNextLevelExperience(CallbackInfoReturnable<Integer> cir) {
        // Use the shadowed field "this.experienceLevel"
        int currentLevel = this.experienceLevel;

        // New formula: (m * Step) + b
        cir.setReturnValue((Config.XP_GROWTH_FACTOR * Math.clamp(Math.round(Math.floor((double) currentLevel / Config.XP_LEVEL_BRACKET_SIZE)), 1, Integer.MAX_VALUE)) + Config.XP_GROWTH_Y_OFFSET);
    }

    /**
     * Prevents the player's level from increasing past Config.XP_MAX_LEVEL.
     * <p>
     * It intercepts the call to addExperienceLevels(int levels) and
     * cancels it *only if* the player is gaining levels (levels > 0)
     * and their current level is already at or above Config.XP_MAX_LEVEL.
     * <p>
     * If Config.XP_MAX_LEVEL is 0 or less, this level cap is disabled.
     */
    @Inject(method = "addExperienceLevels(I)V", at = @At("HEAD"), cancellable = true)
    private void onAddExperienceLevels(int levels, CallbackInfo ci) {
        // If the max level cap is disabled (set to 0 or less), do nothing.
        if (Config.XP_MAX_LEVEL <= 0) {
            return;
        }

        // Only apply the cap if the player is *gaining* levels (levels > 0)
        // and they are already at or above the cap.
        if (levels > 0 && this.experienceLevel >= Config.XP_MAX_LEVEL) {
            ci.cancel(); // Stop the method, preventing the level from increasing
        }

        // If levels < 0 (losing levels), the original method runs normally.
        // If levels > 0 and level < Config.XP_MAX_LEVEL, the original method runs normally.
    }
}
