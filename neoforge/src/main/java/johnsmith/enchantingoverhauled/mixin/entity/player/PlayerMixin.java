package johnsmith.enchantingoverhauled.mixin.entity.player;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import johnsmith.enchantingoverhauled.config.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to {@link Player} to adjust core mechanics related to experience and tool efficiency.
 * <p>
 * This class implements:
 * <ul>
 * <li><b>Efficiency Scaling:</b> Adjusts the calculation of the Efficiency enchantment boost to
 * remain viable even if the maximum enchantment level is capped at 1 or 2.</li>
 * <li><b>XP Curve:</b> Flattens or modifies the experience requirement per level to support
 * the mod's new progression system (often linearizing it).</li>
 * <li><b>Level Cap:</b> Enforces a hard limit on the player's experience level.</li>
 * </ul>
 */
@Mixin(Player.class)
public class PlayerMixin {

    @Shadow
    public int experienceLevel;

    /**
     * Modifies the efficiency calculation formula in {@code getDigSpeed} (Forge/NeoForge naming).
     * <p>
     * Vanilla formula is roughly {@code i * i + 1} (where i is level).
     * This mixin intercepts the {@code i * i} expression.
     * <p>
     * If the max enchantment level is configured to be very low (1 or 2), the vanilla formula
     * provides insufficient speed bonuses. This override boosts the calculation in those cases:
     * <ul>
     * <li><b>Max Level 1:</b> {@code bonus = 27 * i}</li>
     * <li><b>Max Level 2:</b> {@code bonus = (i + 1)^3}</li>
     * <li><b>Otherwise:</b> Standard behavior ({@code i * i}).</li>
     * </ul>
     *
     * @param original The original value of the expression ({@code i * i}).
     * @param i The efficiency level (captured via Local).
     * @return The modified efficiency bonus value.
     */
    @Definition(id = "i", local = @Local(type = int.class))
    @Expression("i * i")
    @ModifyExpressionValue(method = "getDigSpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)F", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int modifyEfficiencyCalculation(int original, @Local(type = int.class) int i) {
        int bonus = original;
        switch (Config.ENCHANTMENT_MAX_LEVEL) {
            case 1: bonus = 27 * i; break;
            case 2: bonus = (int)Math.pow(i + 1, 3); break;
            default: bonus *= i; break;
        } return bonus;
    }

    /**
     * Overrides the XP required to reach the next level.
     * <p>
     * Vanilla uses a curve that gets exponentially steeper. This mod uses a "bracketed"
     * or linear growth system defined by the config to make higher levels more attainable
     * for frequent enchanting.
     *
     * @param cir Callback to set the return value (the XP amount).
     */
    @Inject(method = "getXpNeededForNextLevel", at = @At("HEAD"), cancellable = true)
    private void onGetNextLevelExperience(CallbackInfoReturnable<Integer> cir) {
        int currentLevel = this.experienceLevel;
        // Calculate step based on bracket size
        int step = (int) Math.round(Math.floor((double) currentLevel / Config.XP_LEVEL_BRACKET_SIZE));
        // Linear growth formula: (Slope * BracketStep) + InitialOffset
        cir.setReturnValue((Config.XP_GROWTH_FACTOR * Math.clamp(step, 1, Integer.MAX_VALUE)) + Config.XP_GROWTH_Y_OFFSET);
    }

    /**
     * Prevents the player from gaining levels beyond the configured maximum.
     *
     * @param levels The amount of levels being added.
     * @param ci Callback info to cancel the event if the cap is reached.
     */
    @Inject(method = "giveExperienceLevels", at = @At("HEAD"), cancellable = true)
    private void onAddExperienceLevels(int levels, CallbackInfo ci) {
        if (Config.XP_MAX_LEVEL <= 0) {
            return;
        }
        if (levels > 0 && this.experienceLevel >= Config.XP_MAX_LEVEL) {
            ci.cancel();
        }
    }
}